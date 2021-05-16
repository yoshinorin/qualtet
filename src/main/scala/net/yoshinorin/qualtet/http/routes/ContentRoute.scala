package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.domains.services.ContentService

class ContentRoute(
  contentService: ContentService
) {

  def route: Route = {
    // TODO: change path
    pathPrefix("contents") {
      pathEndOrSingleSlash {
        get {
          // TODO: temp implementation
          val x = contentService.getAll.unsafeRunSync()
          complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${x}")))
        }
      }
    }
  }

}
