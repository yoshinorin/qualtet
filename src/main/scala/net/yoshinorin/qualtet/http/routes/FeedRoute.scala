package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s.*
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed.*
import net.yoshinorin.qualtet.syntax.*

class FeedRoute[M[_]: Monad](
  feedService: FeedService[M]
) {

  def get(name: String): IO[Response[IO]] = {
    for {
      feeds <- feedService.get(ArticlesQueryParameter(1, 5))
      response <- Ok(feeds.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
