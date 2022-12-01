package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.contents.{Content, ContentService, Path, RequestContent}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.contents.ResponseContent._
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.http.{AuthorizationProvider, RequestDecoder}
import net.yoshinorin.qualtet.syntax._

class ContentRoute(
  authorizationProvider: AuthorizationProvider,
  contentService: ContentService
) extends RequestDecoder {

  // contents
  def route: HttpRoutes[IO] = authorizationProvider.authenticate(authedRoute)

  val authedRoute: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case request @ POST -> Root as payload => {
      val maybeContent = for {
        maybeContent <- IO(decode[RequestContent](payload._2))
      } yield maybeContent

      maybeContent.flatMap { c =>
        c match {
          case Left(f) => throw f
          case Right(c) => contentService.createContentFromRequest(payload._1.name, c).flatMap { r =>
            Ok(c.asJson, `Content-Type`(MediaType.application.json))
          }
        }
      }
    }
  }

  /*
  def route: Route = {
    // TODO: logging (who create a content)
    pathPrefix("contents") {

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
