package net.yoshinorin.qualtet.http.routes

import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import cats.effect.IO
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.domains.archives.ResponseArchive._
import net.yoshinorin.qualtet.http.ResponseHandler
import net.yoshinorin.qualtet.syntax._

class ArchiveRoute(
  archiveService: ArchiveService
) extends ResponseHandler {

  // archives
  def route: HttpRoutes[IO] = HttpRoutes.of[IO] {
    { case GET -> Root =>
      for {
        archives <- archiveService.get
        response <- Ok(archives.asJson, `Content-Type`(MediaType.application.json))
      } yield response
    }
  }
}
