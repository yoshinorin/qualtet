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
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class CacheRoute[F[_]: Monad @nowarn](
  authProvider: AuthProvider[F],
  cacheService: CacheService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

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
      _ <- logger.info(s"cache invalidation requested by: ${author.name}")
      _ <- cacheService.invalidateAll()
      response <- NoContent()
    } yield response
  }
}
