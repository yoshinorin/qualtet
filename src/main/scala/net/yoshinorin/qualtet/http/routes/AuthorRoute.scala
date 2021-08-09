package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.authors.AuthorName
import net.yoshinorin.qualtet.domains.services.AuthorService

class AuthorRoute(
  authoreService: AuthorService
) {

  def route: Route = {
    // TODO: authors or users?
    pathPrefix("authors") {
      pathEndOrSingleSlash {
        get {
          // TODO: need fix?
          onSuccess(authoreService.getAll.unsafeToFuture()) { result =>
            complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${result.asJson}")))
          }
        }
      } ~ {
        // example: host/authors/exampleAuthor
        pathPrefix(".+".r) { authorName =>
          pathEndOrSingleSlash {
            get {
              onSuccess(authoreService.findByName(AuthorName(authorName)).unsafeToFuture()) {
                case Some(author) => complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${author.asJson}")))
                case _ => complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, s"TODO: NOT FOUND")))
              }
            }
          }
        }
      }
    }
  }

}
