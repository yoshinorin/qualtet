package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.domains.services.ArticleService
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
            onSuccess(articleService.getWithCount(ArticlesQueryParameter(page, limit)).unsafeToFuture()) { result => httpResponse(OK, result) }
          }
        }
      }
    }
  }

}