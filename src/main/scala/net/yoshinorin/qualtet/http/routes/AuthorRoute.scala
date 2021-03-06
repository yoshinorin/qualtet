package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.http.ResponseHandler

class AuthorRoute(
  authorService: AuthorService
) extends ResponseHandler {

  def route: Route = {
    pathPrefix("authors") {
      pathEndOrSingleSlash {
        get {
          // TODO: need fix?
          onSuccess(authorService.getAll.unsafeToFuture()) { result =>
            complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${result.asJson}")))
          }
        }
      } ~ {
        // example: host/authors/exampleAuthor
        pathPrefix(".+".r) { authorName =>
          pathEndOrSingleSlash {
            get {
              onSuccess(authorService.findByName(AuthorName(authorName)).unsafeToFuture()) {
                case Some(author) => httpResponse(OK, author)
                case _ => httpResponse(Fail.NotFound("Not found"))
              }
            }
          }
        }
      }
    }
  }

}
