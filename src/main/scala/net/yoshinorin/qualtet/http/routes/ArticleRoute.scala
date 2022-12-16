package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount._
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.syntax._

class ArticleRoute(
  articleService: ArticleService
) {

  // articles?page=n&limit=m
  def get(page: Option[Int], limit: Option[Int]): IO[Response[IO]] = {
    for {
      articles <- articleService.getWithCount(ArticlesQueryParameter(page, limit))
      response <- Ok(articles.asJson, `Content-Type`(MediaType.application.json))
      // TODO: error handling (excluded 404)
    } yield response
  }

}
