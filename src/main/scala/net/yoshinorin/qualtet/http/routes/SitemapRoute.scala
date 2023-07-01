package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s.*
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.syntax.*

class SitemapRoute[M[_]: Monad](sitemapService: SitemapService[M]) {

  def get: IO[Response[IO]] = {
    for {
      sitemaps <- sitemapService.get()
      response <- Ok(sitemaps.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
