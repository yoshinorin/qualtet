package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.http.{ArticlesQueryParameter, ResponseHandler}
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed._
import net.yoshinorin.qualtet.syntax._

class FeedRoute(
  feedService: FeedService
) extends ResponseHandler {

  def get(name: String): IO[Response[IO]] = {
    for {
      feeds <- feedService.get(ArticlesQueryParameter(1, 5))
      response <- Ok(feeds.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
