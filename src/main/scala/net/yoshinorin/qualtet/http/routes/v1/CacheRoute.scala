package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.Concurrent
import cats.implicits.*
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{AuthedRoutes, HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.ContextRequest
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.http.AuthProvider
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class CacheRoute[F[_]: Concurrent, G[_]: Monad @nowarn](
  authProvider: AuthProvider[F, G],
  cacheService: CacheService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] = authProvider.authenticate(AuthedRoutes.of { ctxRequest =>
    (ctxRequest match
      case ContextRequest(_, r) =>
        r match
          case request @ DELETE -> Root => this.delete(ctxRequest.context._1)
          case request @ _ => MethodNotAllowed(Allow(Set(DELETE)))
    )
  })

  // caches
  private[http] def delete(author: AuthorResponseModel): F[Response[F]] = {
    for {
      _ <- logger.info(s"cache invalidation requested by: ${author.name}")
      _ <- cacheService.invalidateAll()
      response <- NoContent()
    } yield response
  }
}
