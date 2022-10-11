package net.yoshinorin.qualtet.http.routes


import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import cats.effect.IO
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.domains.archives.ResponseArchive._
import net.yoshinorin.qualtet.http.ResponseHandler

import cats.effect.unsafe.implicits.global

class ArchiveRoute(
  archiveService: ArchiveService
) extends ResponseHandler {

  def route: HttpRoutes[IO] = HttpRoutes.of[IO] {
    {
      // TODO: more smart pathEndOrSlash
      case GET -> Root / "archives" | GET -> Root / "archives" / "" =>
        Ok(makeResonse(archiveService.get.unsafeRunSync()), `Content-Type`(MediaType.application.json))
    }
  }

}
