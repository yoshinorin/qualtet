package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.domains.services.AuthoreService

class AuthorRoute(
  authoreService: AuthoreService
) {

  def route: Route = {
    // TODO: authors or users?
    pathPrefix("authors") {
      pathEndOrSingleSlash {
        get {
          // TODO: need fix?
          onSuccess(authoreService.getAll.unsafeToFuture()) { result =>
            // TODO: toJSON
            complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${result}")))
          }
        }
      }
    }
  }

}
