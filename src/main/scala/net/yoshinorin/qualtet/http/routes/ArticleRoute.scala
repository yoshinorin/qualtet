package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import net.yoshinorin.qualtet.domains.articles.{ArticleService, ResponseArticleWithCount}
import net.yoshinorin.qualtet.domains.models.Fail
import net.yoshinorin.qualtet.http.{ArticlesQueryParameter, RequestDecoder, ResponseHandler}

class ArticleRoute(
  articleService: ArticleService
) extends RequestDecoder
    with ResponseHandler {

  def route: Route = {
    pathPrefix("articles") {
      pathEndOrSingleSlash {
        get {
          parameters("page".as[Int].?, "limit".as[Int].?) { (page, limit) =>
            onSuccess(
              articleService
                .getWithCount(ArticlesQueryParameter(page, limit))
                .handleErrorWith { e => IO.pure(e) }
                .unsafeToFuture()
            ) {
              case r: ResponseArticleWithCount =>
                httpResponse(OK, r)
              case e: Exception =>
                httpResponse(e)
              case _ =>
                httpResponse(Fail.InternalServerError("Internal server error"))
            }
          }
        }
      }
    }
  }

}
