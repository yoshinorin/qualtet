package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed._
import net.yoshinorin.qualtet.syntax._

class FeedRoute[F[_]: Monad](
  feedService: FeedService[F]
) {

  def get(name: String): IO[Response[IO]] = {
    for {
      feeds <- feedService.get(ArticlesQueryParameter(1, 5))
      response <- Ok(feeds.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
