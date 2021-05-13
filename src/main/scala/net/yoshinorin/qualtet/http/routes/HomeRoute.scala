package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Route

class HomeRoute {

  def route: Route = {
    pathEndOrSingleSlash {
      complete(
        HttpResponse(
          200,
          entity = """Hello Qualtet!!""".stripMargin
        )
      )
    }
  }
}
