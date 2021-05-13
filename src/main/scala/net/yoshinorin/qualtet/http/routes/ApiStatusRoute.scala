package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class ApiStatusRoute {

  def route: Route = {
    pathPrefix("status") {
      pathEndOrSingleSlash {
        get {
          complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, "{\"status\":\"operational\"}")))
        }
      }
    }
  }

}
