package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{get, onSuccess, pathEndOrSingleSlash, pathPrefix}
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.http.ResponseHandler

class SitemapRoute(sitemapService: SitemapService) extends ResponseHandler {

  def route: Route = {
    pathPrefix("sitemaps") {
      pathEndOrSingleSlash {
        get {
          onSuccess(sitemapService.get().unsafeToFuture()) { result => httpResponse(OK, result) }
        }
      }
    }
  }

}
