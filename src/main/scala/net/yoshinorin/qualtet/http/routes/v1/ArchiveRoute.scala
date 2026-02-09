package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.*
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class ArchiveRoute[F[_]: Concurrent, G[_]: Monad @nowarn](
  archiveService: ArchiveService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  // archives
  private[http] def get: Request[F] ?=> F[Response[F]] = {
    (for {
      maybeArchives <- EitherT(archiveService.get)
    } yield maybeArchives).value.flatMap {
      case Right(archives) => archives.asResponse(Ok)
      case Left(error: DomainError) => error.asResponse
    }
  }
}
