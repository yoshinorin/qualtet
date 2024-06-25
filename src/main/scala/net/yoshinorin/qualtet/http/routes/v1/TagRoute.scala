package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{AuthedRoutes, HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import org.slf4j.LoggerFactory
import org.http4s.ContextRequest
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.tags.{TagId, TagName, TagService}
import net.yoshinorin.qualtet.http.{ArticlesQueryParameter, AuthProvider}
import net.yoshinorin.qualtet.syntax.*

class TagRoute[F[_]: Monad](
  authProvider: AuthProvider[F],
  tagService: TagService[F],
  articleService: ArticleService[F]
) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private[http] def index: HttpRoutes[IO] =
    tagsWithoutAuth <+>
      authProvider.authenticate(tagsWithAuthed)

  private[http] def tagsWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root =>
      implicit val r = request
      this.get.handleErrorWith(_.logWithStackTrace[IO].andResponse)
    case request @ OPTIONS -> Root =>
      NoContent()
    case request @ GET -> Root / nameOrId =>
      implicit val r = request
      val q = request.uri.query.params.asRequestQueryParamater
      this.get(nameOrId, q.page, q.limit).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def tagsWithAuthed: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of { ctxRequest =>
    implicit val x = ctxRequest.req
    (ctxRequest match {
      case ContextRequest(_, r) =>
        r match {
          case request @ DELETE -> Root / nameOrId => this.delete(nameOrId)
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

  /*
    NOTE:
      The Next.js can not pass custom argument with <Link> component.
      So, I want to belows, but can not...

      - Front-end visible URL: https://example.com/tags/{tagName}
      - API call (when transition with <Link>): https://example.com/tags/{tagId}

      But, it can not. So, I have to find the tagging contents with tagName.
   */
  private[http] def get(nameOrId: String, page: Option[Int], limit: Option[Int]): IO[Response[IO]] = {
    (for {
      articles <- articleService.getByTagNameWithCount(TagName(nameOrId), ArticlesQueryParameter(page, limit))
      response <- Ok(articles.asJson, `Content-Type`(MediaType.application.json))
    } yield response)
  }

  private[http] def delete(nameOrId: String): IO[Response[IO]] = {
    (for {
      _ <- tagService.delete(TagId(nameOrId))
      _ = logger.info(s"deleted tag: ${nameOrId}")
      response <- NoContent()
    } yield response)
  }
}
