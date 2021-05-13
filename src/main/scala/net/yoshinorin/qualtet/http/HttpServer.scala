package net.yoshinorin.qualtet.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.extractClientIP
import net.yoshinorin.qualtet.http.routes.{ApiStatusRoute, HomeRoute}

import scala.concurrent.Future

class HttpServer(
  homeRoute: HomeRoute,
  apiStatusRoute: ApiStatusRoute
)(implicit actorSystem: ActorSystem)
    extends HttpLogger {

  import akka.http.scaladsl.server.RouteConcatenation._

  def start(host: String, port: Int): Future[ServerBinding] = {
    Http().newServerAt(host, port).bind(routes)
  }

  def routes: Route =
    extractClientIP { ip =>
      httpLogging(ip) {
        homeRoute.route ~
          apiStatusRoute.route
      }
    }
}
