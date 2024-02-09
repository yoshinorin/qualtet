package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.http.MethodNotAllowedSupport

class SitemapRoute[F[_]: Monad](sitemapService: SitemapService[F]) extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { r =>
    implicit val x = r
    (r match {
      case request @ GET -> Root => this.get
      case request @ OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
      case request @ _ =>
        methodNotAllowed(request, Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def get: IO[Response[IO]] = {
    for {
      sitemaps <- sitemapService.get()
      response <- Ok(sitemaps.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
