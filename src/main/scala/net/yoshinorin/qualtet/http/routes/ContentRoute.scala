package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.contents.Content
import net.yoshinorin.qualtet.domains.services.ContentService

class ContentRoute(
  contentService: ContentService
) {

  def route: Route = {
    // TODO: change path
    pathPrefix("contents") {
      pathEndOrSingleSlash {
        get {
          // TODO: need fix?
          onSuccess(contentService.getAll.unsafeToFuture()) { result =>
            complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${result.asJson}")))
          }
        } ~ post {
          // TODO
          complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, "TODO")))
        }
      }
    }
  }

}
