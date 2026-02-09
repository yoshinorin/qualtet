package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.Monad
import cats.implicits.*
import cats.effect.Concurrent
import org.http4s.headers.Allow
import org.http4s.{AuthedRoutes, HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.{ContextRequest, Request}
import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.contents.{
  AdjacentContentResponseModel,
  ContentDetailResponseModel,
  ContentId,
  ContentRequestModel,
  ContentResponseModel,
  ContentService
}
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.http.request.Decoder
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class ContentRoute[F[_]: Concurrent, G[_]: Monad @nowarn](
  authProvider: AuthProvider[F, G],
  contentService: ContentService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F])
    extends Decoder[F] {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  // NOTE: must be compose `auth route` after `Non auth route`.
  private[http] def index: HttpRoutes[F] =
    (contentWithoutAuth <+>
      authProvider.authenticate(contentWithAuthed))

  private def removeApiPath(path: String): String = {
    path.replace("/v1/contents/", "")
  }

  private def isAdjacentEndpointRequest(path: String): Boolean = {
    path.endsWith("/adjacent") || path.endsWith("/adjacent/")
  }

  private[http] def contentWithoutAuth: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    // NOTE: Cannot use `GET -> Root / <path>` here because it would no longer pattern match other composed HTTP methods such as `POST` or `DELETE`.
    (r match
      case request @ GET -> _ if isAdjacentEndpointRequest(request.path) =>
        val maybeId = removeApiPath(request.path).replace("adjacent", "").replace("/", "")
        this
          .getAdjacent(maybeId)
          .handleErrorWith(_.logWithStackTrace[F].asResponse)
      case request @ GET -> _ =>
        this
          .get(removeApiPath(request.path))
          .handleErrorWith(_.logWithStackTrace[F].asResponse)
    )
  }

  private[http] def contentWithAuthed: AuthedRoutes[(AuthorResponseModel, String), F] = AuthedRoutes.of { ctxRequest =>
    implicit val x = ctxRequest.req
    (ctxRequest match {
      case ContextRequest(_, r) =>
        r match {
          case request @ POST -> Root => this.post(ctxRequest.context)
          case request @ DELETE -> Root / id => this.delete(id)
          case request @ _ => MethodNotAllowed(Allow(Set(GET, POST, DELETE)))
        }
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  private[http] def post(payload: (AuthorResponseModel, String)): Request[F] ?=> F[Response[F]] = {
    (for {
      maybeDecodedContent <- EitherT(decode[ContentRequestModel](payload._2))
      maybeContent <- EitherT(contentService.createOrUpdate(payload._1.name, maybeDecodedContent))
    } yield maybeContent).value.flatMap {
      case Right(content) => content.asResponse(Created)
      case Left(error: DomainError) => error.asResponse
    }
  }

  private[http] def delete(id: String): Request[F] ?=> F[Response[F]] = {
    (for {
      result <- EitherT(contentService.delete(ContentId(id)))
    } yield result).value.flatMap {
      case Right(_) => logger.info(s"deleted content: ${id}") *> NoContent()
      case Left(error: DomainError) => error.asResponse
    }
  }

  def get(path: String): Request[F] ?=> F[Response[F]] = {
    (for {
      maybeContentPath <- EitherT.fromEither[F](ContentPath(path))
      maybeContent <- EitherT(contentService.findByPathWithMeta(maybeContentPath))
    } yield maybeContent).value.flatMap {
      case Right(content) => content.asResponse
      case Left(error: DomainError) => error.asResponse
    }
  }

  def getAdjacent(id: String): Request[F] ?=> F[Response[F]] = {
    (for {
      maybeContent <- EitherT.liftF(contentService.findById(ContentId(id)))
      adjacentOpt <- maybeContent match {
        case Some(content) => EitherT(contentService.findAdjacent(content.id))
        case None => EitherT.rightT[F, DomainError](None)
      }
    } yield adjacentOpt).value.flatMap {
      case Right(adjacent) => adjacent.asResponse
      case Left(error: DomainError) => error.asResponse
    }
  }

}
