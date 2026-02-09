package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.Monad
import cats.implicits.*
import cats.effect.IO
import org.http4s.headers.Allow
import org.http4s.{AuthedRoutes, HttpRoutes, Response}
import org.http4s.dsl.io.*
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

class ContentRoute[G[_]: Monad @nowarn](
  authProvider: AuthProvider[G],
  contentService: ContentService[IO, G]
)(using loggerFactory: Log4CatsLoggerFactory[IO])
    extends Decoder[IO] {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  // NOTE: must be compose `auth route` after `Non auth route`.
  private[http] def index: HttpRoutes[IO] =
    (contentWithoutAuth <+>
      authProvider.authenticate(contentWithAuthed))

  private def removeApiPath(path: String): String = {
    path.replace("/v1/contents/", "")
  }

  private def isAdjacentEndpointRequest(path: String): Boolean = {
    path.endsWith("/adjacent") || path.endsWith("/adjacent/")
  }

  private[http] def contentWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    // NOTE: Cannot use `GET -> Root / <path>` here because it would no longer pattern match other composed HTTP methods such as `POST` or `DELETE`.
    (r match
      case request @ GET -> _ if isAdjacentEndpointRequest(request.path) =>
        val maybeId = removeApiPath(request.path).replace("adjacent", "").replace("/", "")
        this
          .getAdjacent(maybeId)
          .handleErrorWith(_.logWithStackTrace[IO].asResponse)
      case request @ GET -> _ =>
        this
          .get(removeApiPath(request.path))
          .handleErrorWith(_.logWithStackTrace[IO].asResponse)
    )
  }

  private[http] def contentWithAuthed: AuthedRoutes[(AuthorResponseModel, String), IO] = AuthedRoutes.of { ctxRequest =>
    implicit val x = ctxRequest.req
    (ctxRequest match {
      case ContextRequest(_, r) =>
        r match {
          case request @ POST -> Root => this.post(ctxRequest.context)
          case request @ DELETE -> Root / id => this.delete(id)
          case request @ _ => MethodNotAllowed(Allow(Set(GET, POST, DELETE)))
        }
    }).handleErrorWith(_.logWithStackTrace[IO].asResponse)
  }

  private[http] def post(payload: (AuthorResponseModel, String)): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      maybeDecodedContent <- EitherT(decode[ContentRequestModel](payload._2))
      maybeContent <- EitherT(contentService.createOrUpdate(payload._1.name, maybeDecodedContent))
    } yield maybeContent).value.flatMap {
      case Right(content) => content.asResponse(Created)
      case Left(error: DomainError) => error.asResponse
    }
  }

  private[http] def delete(id: String): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      result <- EitherT(contentService.delete(ContentId(id)))
    } yield result).value.flatMap {
      case Right(_) => logger.info(s"deleted content: ${id}") *> NoContent()
      case Left(error: DomainError) => error.asResponse
    }
  }

  def get(path: String): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      maybeContentPath <- EitherT.fromEither[IO](ContentPath(path))
      maybeContent <- EitherT(contentService.findByPathWithMeta(maybeContentPath))
    } yield maybeContent).value.flatMap {
      case Right(content) => content.asResponse
      case Left(error: DomainError) => error.asResponse
    }
  }

  def getAdjacent(id: String): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      maybeContent <- EitherT.liftF(contentService.findById(ContentId(id)))
      adjacentOpt <- maybeContent match {
        case Some(content) => EitherT(contentService.findAdjacent(content.id))
        case None => EitherT.rightT[IO, DomainError](None)
      }
    } yield adjacentOpt).value.flatMap {
      case Right(adjacent) => adjacent.asResponse
      case Left(error: DomainError) => error.asResponse
    }
  }

}
