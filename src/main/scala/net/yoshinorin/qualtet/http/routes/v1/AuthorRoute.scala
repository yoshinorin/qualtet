package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.Request
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class AuthorRoute[G[_]: Monad @nowarn](
  authorService: AuthorService[G, IO]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ GET -> Root / authorName => this.get(authorName)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].asResponse)
  }

  // authors
  private[http] def get: IO[Response[IO]] = {
    for {
      authors <- authorService.getAll
      response <- authors.asResponse(Ok)
    } yield response
  }

  private[http] def get(authorName: String): Request[IO] ?=> IO[Response[IO]] = {
    AuthorName(authorName) match {
      case Right(name) =>
        authorService.findByName(name).flatMap(_.asResponse)
      case Left(error) =>
        error.asResponse
    }
  }

}
