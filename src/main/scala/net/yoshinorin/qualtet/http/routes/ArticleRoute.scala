package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.data.OptionT
import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.domains.articles.{ArticleService, ResponseArticleWithCount}
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount._
import net.yoshinorin.qualtet.http.{ArticlesQueryParameter, ResponseHandler}
import net.yoshinorin.qualtet.syntax._

// TODO: move somewhere
object PageQueryParam extends OptionalQueryParamDecoderMatcher[Int]("page")
object LimitQueryParam extends OptionalQueryParamDecoderMatcher[Int]("limit")

class ArticleRoute(
  articleService: ArticleService
) extends ResponseHandler {

  // articles?page=n&limit=m
  def route: HttpRoutes[IO] = HttpRoutes[IO] {
    { case GET -> Root :? PageQueryParam(page) +& LimitQueryParam(limit) =>
      for {
        articles <- OptionT.liftF(articleService.getWithCount(ArticlesQueryParameter(page, limit)))
        response <- OptionT.liftF(Ok(articles.asJson, `Content-Type`(MediaType.application.json)))
        // TODO: error handling (excluded 404)
      } yield response
    }
  }

}
