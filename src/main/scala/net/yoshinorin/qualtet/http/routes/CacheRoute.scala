package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{AuthedRoutes, HttpRoutes, Response}
import org.http4s.dsl.io.*
import org.http4s.ContextRequest
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.http.{AuthProvider, MethodNotAllowedSupport}

class CacheRoute[F[_]: Monad](
  authProvider: AuthProvider[F],
  cacheService: CacheService[F]
) extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = authProvider.authenticate(AuthedRoutes.of { ctxRequest =>
    (ctxRequest match
      case ContextRequest(_, r) =>
        r match
          case request @ DELETE -> Root => this.delete(ctxRequest.context._1)
          case request @ _ =>
            methodNotAllowed(r, Allow(Set(DELETE)))
    )
  })

  // caches
  private[http] def delete(author: ResponseAuthor): IO[Response[IO]] = {
    for {
      _ <- cacheService.invalidateAll()
      response <- NoContent()
    } yield response
  }
}
