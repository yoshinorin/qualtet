package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{AuthedRoutes, HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import org.http4s.ContextRequest
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.tags.{TagId, TagPath, TagService}
import net.yoshinorin.qualtet.domains.PaginationRequestModel
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class TagRoute[F[_]: Monad](
  authProvider: AuthProvider[F],
  tagService: TagService[F],
  articleService: ArticleService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] =
    tagsWithoutAuth <+>
      authProvider.authenticate(tagsWithAuthed)

  private[http] def tagsWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    r match {
      case request @ GET -> Root =>
        this.get.handleErrorWith(_.logWithStackTrace[IO].andResponse)
      case request @ OPTIONS -> Root =>
        NoContent()
      case request @ GET -> Root / tagPath =>
        val p = request.uri.query.params.asPagination
        this.get(tagPath, p).handleErrorWith(_.logWithStackTrace[IO].andResponse)
    }
  }

  private[http] def tagsWithAuthed: AuthedRoutes[(AuthorResponseModel, String), IO] = AuthedRoutes.of { ctxRequest =>
    implicit val x = ctxRequest.req
    (ctxRequest match {
      case ContextRequest(_, r) =>
        r match {
          case request @ DELETE -> Root / id => this.delete(id)
          case request @ _ => MethodNotAllowed(Allow(Set(GET, DELETE)))
        }
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def get: IO[Response[IO]] = {
    for {
      allTags <- tagService.getAll
      response <- Ok(allTags.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

  private[http] def get(path: String, p: PaginationRequestModel): IO[Response[IO]] = {
    (for {
      tagPath <- TagPath(path).liftTo[IO]
      articles <- articleService.getByTagPathWithCount(tagPath, p)
      response <- Ok(articles.asJson, `Content-Type`(MediaType.application.json))
    } yield response)
  }

  private[http] def delete(id: String): IO[Response[IO]] = {
    (for {
      _ <- tagService.delete(TagId(id))
      _ = logger.info(s"deleted tag: ${id}")
      response <- NoContent()
    } yield response)
  }
}
