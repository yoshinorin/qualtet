package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.services.ArticleService
import net.yoshinorin.qualtet.http.{ArticlesQueryParameter, RequestDecoder}

class ArticleRoute(
  articleService: ArticleService
) extends RequestDecoder {

  def route: Route = {
    pathPrefix("articles") {
      pathEndOrSingleSlash {
        get {
          parameters("page".as[Int].?, "limit".as[Int].?) { (page, limit) =>
            onSuccess(articleService.getWithCount(ArticlesQueryParameter(page, limit)).unsafeToFuture()) { result =>
              complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${result.asJson}")))
            }
          }
        }
      }
    }
  }

}
