package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s.*
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.domains.archives.ResponseArchive.*
import net.yoshinorin.qualtet.syntax.*

class ArchiveRoute[M[_]: Monad](
  archiveService: ArchiveService[M]
) {

  // archives
  def get: IO[Response[IO]] = {
    for {
      archives <- archiveService.get
      response <- Ok(archives.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }
}
