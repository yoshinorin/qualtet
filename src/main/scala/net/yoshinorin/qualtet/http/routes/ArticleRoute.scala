package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s.*
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount.*
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.syntax.*

class ArticleRoute[M[_]: Monad](
  articleService: ArticleService[M]
) {

  // articles?page=n&limit=m
  def get(page: Option[Int], limit: Option[Int]): IO[Response[IO]] = {
    (for {
      articles <- articleService.getWithCount(ArticlesQueryParameter(page, limit))
      response <- Ok(articles.asJson, `Content-Type`(MediaType.application.json))
    } yield response).handleErrorWith(_.logWithStackTrace.andResponse)
  }

}
