package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.cache.CacheService

class CacheRoute[F[_]: Monad](
  cacheService: CacheService[F]
) {

  // caches
  def delete(author: ResponseAuthor): IO[Response[IO]] = {
    for {
      _ <- cacheService.invalidateAll()
      response <- NoContent()
    } yield response
  }
}
