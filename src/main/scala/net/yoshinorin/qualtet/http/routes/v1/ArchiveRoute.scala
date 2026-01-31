package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.effect.IO
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class ArchiveRoute[F[_]: Monad @nowarn](
  archiveService: ArchiveService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].asResponse)
  }

  // archives
  private[http] def get: Request[IO] ?=> IO[Response[IO]] = {
    (for {
      maybeArchives <- EitherT(archiveService.get)
    } yield maybeArchives).value.flatMap {
      case Right(archives) => archives.asResponse(Ok)
      case Left(error: DomainError) => error.asResponse
    }
  }
}
