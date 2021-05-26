package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.contents.{Content, RequestContent}
import net.yoshinorin.qualtet.domains.services.ContentService
import net.yoshinorin.qualtet.http.RequestDecoder

class ContentRoute(
  contentService: ContentService
) extends RequestDecoder {

  import Content._

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
          entity(as[String]) { payload =>
            decode[RequestContent](payload) match {
              case Right(v) =>
                /*
                  TODO:
                   Find UserId
                   Find ContentId
                   RequestContent to Content
                   Insert Contents TABLE
                 */
                complete(HttpResponse(Created, entity = HttpEntity(ContentTypes.`application/json`, s"${v.asJson}")))
              case Left(message) =>
                complete(HttpResponse(BadRequest, entity = HttpEntity(ContentTypes.`application/json`, s"${message.asJson}")))
            }
          }
        }
      }
    }
  }

}
