package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.*
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.cache.CacheService

class CacheRoute[M[_]: Monad](
  cacheService: CacheService[M]
) {

  // caches
  def delete(author: ResponseAuthor): IO[Response[IO]] = {
    for {
      _ <- cacheService.invalidateAll()
      response <- NoContent()
    } yield response
  }
}
