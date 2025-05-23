package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{AuthedRoutes, HttpRoutes, Response}
import org.http4s.dsl.io.*
import org.http4s.ContextRequest
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.http.AuthProvider

class CacheRoute[F[_]: Monad](
  authProvider: AuthProvider[F],
  cacheService: CacheService[F]
) {

  private[http] def index: HttpRoutes[IO] = authProvider.authenticate(AuthedRoutes.of { ctxRequest =>
    (ctxRequest match
      case ContextRequest(_, r) =>
        r match
          case request @ DELETE -> Root => this.delete(ctxRequest.context._1)
          case request @ _ => MethodNotAllowed(Allow(Set(DELETE)))
    )
  })

  // caches
  private[http] def delete(author: AuthorResponseModel): IO[Response[IO]] = {
    for {
      _ <- cacheService.invalidateAll()
      response <- NoContent()
    } yield response
  }
}
