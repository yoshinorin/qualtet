package net.yoshinorin.qualtet.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.extractClientIP
import net.yoshinorin.qualtet.http.routes.{
  ApiStatusRoute,
  ArchiveRoute,
  ArticleRoute,
  AuthRoute,
  AuthorRoute,
  ContentRoute,
  ContentTypeRoute,
  HomeRoute,
  SitemapRoute
}

import scala.concurrent.Future

class HttpServer(
  homeRoute: HomeRoute,
  apiStatusRoute: ApiStatusRoute,
  authRoute: AuthRoute,
  authorRoute: AuthorRoute,
  contentRoute: ContentRoute,
  articleRoute: ArticleRoute,
  archiveRoute: ArchiveRoute,
  contentTypeRoute: ContentTypeRoute,
  sitemapRoute: SitemapRoute
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
          apiStatusRoute.route ~
          authRoute.route ~
          authorRoute.route ~
          contentRoute.route ~
          articleRoute.route ~
          archiveRoute.route ~
          contentTypeRoute.route ~
          sitemapRoute.route
      }
    }
}
