package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import cats.effect.IO
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.models.Fail
import net.yoshinorin.qualtet.domains.models.contents.{Content, Path, RequestContent}
import net.yoshinorin.qualtet.domains.services.ContentService
import net.yoshinorin.qualtet.http.{Authentication, RequestDecoder, ResponseHandler}

class ContentRoute(
  authService: AuthService,
  contentService: ContentService
) extends Authentication(authService)
    with RequestDecoder
    with ResponseHandler {

  def route: Route = {
    // TODO: logging (who create a content)
    pathPrefix("contents") {
      pathEndOrSingleSlash {
        post {
          authenticate { author =>
            entity(as[String]) { payload =>
              decode[RequestContent](payload) match {
                case Right(v) =>
                  onSuccess(
                    contentService
                      .createContentFromRequest(author.name, v)
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
        }
      } ~ {
        // NOTE: need slash on the prefix but it is required on the suffix
        // example: /yyyy/mm/dd/content-name/
        path(Remaining) { path =>
          get {
            onSuccess(contentService.findByPathWithMeta(Path(path)).unsafeToFuture()) {
              case Some(content) =>
                httpResponse(OK, content)
              case _ => httpResponse(Fail.NotFound("Not found"))
            }
          }
        }
      }
    }
  }

}
