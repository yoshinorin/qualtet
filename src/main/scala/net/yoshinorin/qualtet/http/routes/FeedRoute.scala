package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{get, onSuccess, pathPrefix}
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.http.{ArticlesQueryParameter, ResponseHandler}

class FeedRoute(
  articleService: ArticleService
) extends ResponseHandler {

  def route: Route = {
    pathPrefix("feeds") {
      pathPrefix(".+".r) { _ =>
        get {
          // TOOD: configurable
          // TODO: create feed by x (e.g. tagName
          onSuccess(articleService.getFeeds(ArticlesQueryParameter(1, 5)).unsafeToFuture()) { result => httpResponse(OK, result) }
        }
      }
    }
  }

}
