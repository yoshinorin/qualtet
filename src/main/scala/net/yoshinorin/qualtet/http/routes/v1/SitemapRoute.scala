package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class SitemapRoute[F[_]: Monad](sitemapService: SitemapService[F])(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].asResponse)
  }

  private[http] def get: IO[Response[IO]] = {
    for {
      sitemaps <- sitemapService.get()
      response <- sitemaps.asResponse(Ok)
    } yield response
  }

}
