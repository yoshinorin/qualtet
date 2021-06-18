package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.Fail
import net.yoshinorin.qualtet.domains.services.ArticleService
import net.yoshinorin.qualtet.http.RequestDecoder

class ArticleRoute(
  articleService: ArticleService
) extends RequestDecoder {

  def route: Route = {
    pathPrefix("articles") {
      pathEndOrSingleSlash {
        get {
          onSuccess(articleService.getAll.unsafeToFuture()) { result =>
            complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${result.asJson}")))
          }
        }
      }
    }
  }

}
