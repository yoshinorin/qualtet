package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.contents.{Content, ContentService, Path, RequestContent}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.contents.ResponseContent._
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.http.{Authentication, RequestDecoder, ResponseHandler}

class ContentRoute(
  authService: AuthService,
  contentService: ContentService
) extends Authentication(authService)
    with RequestDecoder
    with ResponseHandler {

  // contents
  /*
  def route: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root => {
      for {
        stringifyRequest <- request.as[String]
        _ <- contentService.createContentFromRequest()
      }
    }
  }
  */

  /*
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
        pathPrefix(".+".r) { id =>
          pathEndOrSingleSlash {
            delete {
              authenticate { _ =>
                onSuccess(
                  contentService
                    .delete(ContentId(id))
                    .handleErrorWith { e => IO.pure(e) }
                    .unsafeToFuture()
                ) {
                  case e: Exception =>
                    httpResponse(e)
                  case _ => httpResponse(NoContent)
                }
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
  */

}
