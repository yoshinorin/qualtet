package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import cats.effect.IO
import net.yoshinorin.qualtet.domains.models.Fail
import net.yoshinorin.qualtet.domains.models.contents.{Content, Path, RequestContent, ResponseContent}
import net.yoshinorin.qualtet.domains.services.ContentService
import net.yoshinorin.qualtet.http.{RequestDecoder, ResponseHandler}

class ContentRoute(
  contentService: ContentService
) extends RequestDecoder
    with ResponseHandler {

  def route: Route = {
    // TODO: change path
    pathPrefix("contents") {
      pathEndOrSingleSlash {
        post {
          entity(as[String]) { payload =>
            decode[RequestContent](payload) match {
              case Right(v) =>
                onSuccess(
                  contentService
                    .createContentFromRequest(v)
                    .handleErrorWith { e => IO.pure(e) }
                    .unsafeToFuture()
                ) {
                  case c: Content =>
                    httpResponse(Created, c)
                  case e: Exception =>
                    httpResponse(e)
                  case _ =>
                    httpResponse(Fail.InternalServerError("Internal server error"))
                }
              case Left(message) =>
                httpResponse(message)
            }
          }
        }
      } ~ {
        // NOTE: need slash on the prefix but it is required on the suffix
        // example: /yyyy/mm/dd/content-name/
        path(Remaining) { path =>
          get {
            onSuccess(contentService.findByPath(Path(path)).unsafeToFuture()) {
              case Some(content) =>
                httpResponse(OK, ResponseContent(content.title, content.htmlContent, content.publishedAt))
              case _ => httpResponse(Fail.NotFound("Not found"))
            }
          }
        }
      }
    }
  }

}
