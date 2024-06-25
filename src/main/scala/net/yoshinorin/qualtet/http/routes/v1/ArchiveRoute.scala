package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.syntax.*

class ArchiveRoute[F[_]: Monad](
  archiveService: ArchiveService[F]
) {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { r =>
    implicit val x = r
    (r match {
      case request @ GET -> Root => this.get
      case request @ OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  // archives
  private[http] def get: IO[Response[IO]] = {
    for {
      archives <- archiveService.get
      response <- Ok(archives.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }
}
