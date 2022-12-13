package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.cache.CacheService

class CacheRoute(
  authProvider: AuthProvider,
  cacheService: CacheService
) {

  def route: HttpRoutes[IO] = authProvider.authenticate(authedRoutes)
  // caches
  val authedRoutes: AuthedRoutes[(ResponseAuthor, String), IO] =
    AuthedRoutes.of { case DELETE -> Root as author =>
      for {
        _ <- cacheService.invalidateAll()
        response <- NoContent()
      } yield response
    }

  def delete(author: ResponseAuthor): IO[Response[IO]] = {
    for {
      _ <- cacheService.invalidateAll()
      response <- NoContent()
    } yield response
  }
}
