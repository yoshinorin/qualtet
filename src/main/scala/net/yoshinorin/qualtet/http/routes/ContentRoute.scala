package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.Fail
import net.yoshinorin.qualtet.domains.models.contents.{Content, RequestContent, ResponseContent}
import net.yoshinorin.qualtet.domains.services.ContentService
import net.yoshinorin.qualtet.http.RequestDecoder

class ContentRoute(
  contentService: ContentService
) extends RequestDecoder {

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
                    complete(HttpResponse(Created, entity = HttpEntity(ContentTypes.`application/json`, s"${c.asJson}")))
                  case f: Fail =>
                    complete(HttpResponse(UnprocessableEntity, entity = HttpEntity(ContentTypes.`application/json`, s"${f.asJson}")))
                  case _ =>
                    // TODO: create Internal server error case class
                    complete(HttpResponse(InternalServerError, entity = HttpEntity(ContentTypes.`application/json`, s"Internal server error")))
                }
              case Left(message) =>
                complete(HttpResponse(BadRequest, entity = HttpEntity(ContentTypes.`application/json`, s"${message.asJson}")))
            }
          }
        }
      } ~ {
        // NOTE: no-need slash on the prefix but it is required on the suffix
        // example: yyyy/mm/dd/content-name/
        path(Remaining) { path =>
          get {
            onSuccess(contentService.findByPath(path).unsafeToFuture()) {
              case Some(content) =>
                complete(
                  HttpResponse(
                    OK,
                    entity = HttpEntity(ContentTypes.`application/json`, s"${ResponseContent(content.title, content.htmlContent, content.publishedAt).asJson}")
                  )
                )
              case _ => complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, s"TODO: NOT FOUND")))
            }
          }
        }
      }
    }
  }

}
