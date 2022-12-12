package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.implicits._
import cats.data.Kleisli
import cats.data.OptionT
import org.http4s.HttpRoutes
import org.http4s.server._
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.http.AuthorizationProvider
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.cache.CacheService

class CacheRoute(
  authorizationProvider: AuthorizationProvider,
  cacheService: CacheService
) {

  def route: HttpRoutes[IO] = authorizationProvider.authenticate(authedRoutes)
  // caches
  val authedRoutes: AuthedRoutes[(ResponseAuthor, String), IO] =
    AuthedRoutes.of { case DELETE -> Root as author =>
      for {
        _ <- cacheService.invalidateAll()
        response <- NoContent()
      } yield response
    }

  def delete(author: ResponseAuthor) = {
    for {
      _ <- cacheService.invalidateAll()
      response <- NoContent()
    } yield response
  }
}
