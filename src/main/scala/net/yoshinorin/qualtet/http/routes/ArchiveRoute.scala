package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.domains.archives.ResponseArchive._
import net.yoshinorin.qualtet.syntax._

class ArchiveRoute[F[_]: Monad](
  archiveService: ArchiveService[F]
) {

  // archives
  def get: IO[Response[IO]] = {
    for {
      archives <- archiveService.get
      response <- Ok(archives.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }
}
