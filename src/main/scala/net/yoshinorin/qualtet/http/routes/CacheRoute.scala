package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{AuthedRoutes, HttpRoutes, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.http.{AuthProvider, MethodNotAllowedSupport}

class CacheRoute[M[_]: Monad](
  authProvider: AuthProvider[M],
  cacheService: CacheService[M]
) extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = authProvider.authenticate(AuthedRoutes.of {
    case DELETE -> Root as author => this.delete(author._1)
    case request @ _ =>
      methodNotAllowed(request.req, Allow(Set(DELETE)))
  })

  // caches
  private[http] def delete(author: ResponseAuthor): IO[Response[IO]] = {
    for {
      _ <- cacheService.invalidateAll()
      response <- NoContent()
    } yield response
  }
}
