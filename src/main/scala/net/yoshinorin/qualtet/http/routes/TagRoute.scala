package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.Response
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.tags.{TagId, TagName, TagService}
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.syntax._

class TagRoute(
  tagService: TagService,
  articleService: ArticleService
) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def get: IO[Response[IO]] = {
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
  def get(nameOrId: String, page: Option[Int], limit: Option[Int]): IO[Response[IO]] = {
    (for {
      articles <- articleService.getByTagNameWithCount(TagName(nameOrId), ArticlesQueryParameter(page, limit))
      response <- Ok(articles.asJson, `Content-Type`(MediaType.application.json))
    } yield response).handleErrorWith(_.asResponse)
  }

  def delete(nameOrId: String): IO[Response[IO]] = {
    (for {
      _ <- tagService.delete(TagId(nameOrId))
      _ = logger.info(s"deleted tag: ${nameOrId}")
      response <- NoContent()
    } yield response).handleErrorWith(_.asResponse)
  }
}
