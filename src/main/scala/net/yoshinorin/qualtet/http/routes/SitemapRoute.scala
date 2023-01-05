package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.syntax._

class SitemapRoute[F[_]: Monad](sitemapService: SitemapService[F]) {

  def get: IO[Response[IO]] = {
    for {
      sitemaps <- sitemapService.get()
      response <- Ok(sitemaps.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
