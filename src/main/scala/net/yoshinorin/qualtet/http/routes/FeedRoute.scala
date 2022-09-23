package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{get, onSuccess, pathPrefix}
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.http.{ArticlesQueryParameter, ResponseHandler}
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed._

import cats.effect.unsafe.implicits.global

class FeedRoute(
  feedService: FeedService
) extends ResponseHandler {

  def route: Route = {
    pathPrefix("feeds") {
      pathPrefix(".+".r) { _ =>
        get {
          // TOOD: configurable
          // TODO: create feed by x (e.g. tagName
          onSuccess(feedService.get(ArticlesQueryParameter(1, 5)).unsafeToFuture()) { result => httpResponseWithJsoniter(OK, result) }
        }
      }
    }
  }

}
