package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.effect.Concurrent
import cats.Monad
import cats.implicits.*
import org.http4s.headers.Allow
import org.http4s.{AuthedRoutes, HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.ContextRequest
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.tags.{TagId, TagPath, TagService}
import net.yoshinorin.qualtet.domains.PaginationRequestModel
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class TagRoute[F[_]: Concurrent, G[_]: Monad @nowarn](
  authProvider: AuthProvider[F, G],
  tagService: TagService[F, G],
  articleService: ArticleService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] =
    tagsWithoutAuth <+>
      authProvider.authenticate(tagsWithAuthed)

  private[http] def tagsWithoutAuth: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    r match {
      case request @ GET -> Root =>
        this.get.handleErrorWith(_.logWithStackTrace[F].asResponse)
      case request @ OPTIONS -> Root =>
        NoContent()
      case request @ GET -> Root / tagPath =>
        val p = request.uri.query.params.asPagination
        this.get(tagPath, p).handleErrorWith(_.logWithStackTrace[F].asResponse)
    }
  }

  private[http] def tagsWithAuthed: AuthedRoutes[(AuthorResponseModel, String), F] = AuthedRoutes.of { ctxRequest =>
    implicit val x = ctxRequest.req
    (ctxRequest match {
      case ContextRequest(_, r) =>
        r match {
          case request @ DELETE -> Root / id => this.delete(id)
          case request @ _ => MethodNotAllowed(Allow(Set(GET, DELETE)))
        }
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  private[http] def get: F[Response[F]] = {
    for {
      allTags <- tagService.getAll
      response <- allTags.asResponse(Ok)
    } yield response
  }

  private[http] def get(path: String, p: PaginationRequestModel): Request[F] ?=> F[Response[F]] = {
    (for {
      tagPath <- EitherT.fromEither[F](TagPath(path))
      articles <- EitherT(articleService.getByTagPathWithCount(tagPath, p))
    } yield articles).value.flatMap {
      case Right(articles) => articles.asResponse(Ok)
      case Left(error: DomainError) => error.asResponse
    }
  }

  private[http] def delete(id: String): Request[F] ?=> F[Response[F]] = {
    (for {
      _ <- EitherT(tagService.delete(TagId(id)))
    } yield ()).value.flatMap {
      case Right(_) => logger.info(s"deleted tag: ${id}") *> NoContent()
      case Left(error: DomainError) => error.asResponse
    }
  }
}
