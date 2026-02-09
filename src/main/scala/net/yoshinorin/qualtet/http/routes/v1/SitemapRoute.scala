package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.Concurrent
import cats.implicits.*
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class SitemapRoute[F[_]: Concurrent, G[_]: Monad @nowarn](sitemapService: SitemapService[F, G])(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  private[http] def get: F[Response[F]] = {
    for {
      sitemaps <- sitemapService.get()
      response <- sitemaps.asResponse(Ok)
    } yield response
  }

}
