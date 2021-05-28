package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.services.ContentTypeService

class ContentTypeRoute(
  contentTypeService: ContentTypeService
) {

  def route: Route = {
    // TODO: use underscore instead of hyphen?
    pathPrefix("content-types") {
      pathEndOrSingleSlash {
        get {
          onSuccess(contentTypeService.getAll.unsafeToFuture()) { result =>
            complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${result.asJson}")))
          }
        }
      } ~ {
        // example: host/content-types/article
        pathPrefix(".+".r) { conetntTypeName =>
          pathEndOrSingleSlash {
            get {
              onSuccess(contentTypeService.findByName(conetntTypeName).unsafeToFuture()) {
                case Some(contentType) => complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${contentType.asJson}")))
                case _ => complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, s"TODO: NOT FOUND")))
              }
            }
          }
        }
      }
    }
  }

}
