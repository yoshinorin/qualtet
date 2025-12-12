package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class ArchiveRoute[F[_]: Monad](
  archiveService: ArchiveService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  // archives
  private[http] def get: IO[Response[IO]] = {
    for {
      getResult <- archiveService.get
      archives <- getResult.liftTo[IO]
      response <- Ok(archives.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }
}
