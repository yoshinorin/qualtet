package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.http.ResponseHandler

class ContentTypeRoute(
  contentTypeService: ContentTypeService
) extends ResponseHandler {

  def route: Route = {
    // TODO: use underscore instead of hyphen?
    pathPrefix("content-types") {
      pathEndOrSingleSlash {
        get {
          onSuccess(contentTypeService.getAll.unsafeToFuture()) { result => httpResponse(OK, result) }
        }
      } ~ {
        // example: host/content-types/article
        pathPrefix(".+".r) { conetntTypeName =>
          pathEndOrSingleSlash {
            get {
              onSuccess(contentTypeService.findByName(conetntTypeName).unsafeToFuture()) {
                case Some(contentType) =>
                  httpResponse(OK, contentType)
                case _ => httpResponse(Fail.NotFound("Not found"))
              }
            }
          }
        }
      }
    }
  }

}
