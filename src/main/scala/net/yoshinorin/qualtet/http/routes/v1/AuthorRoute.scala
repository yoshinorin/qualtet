package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.implicits.*
import cats.Monad
import org.http4s.Request
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class AuthorRoute[F[_]: Monad](
  authorService: AuthorService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ GET -> Root / authorName => this.get(authorName)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  // authors
  private[http] def get: IO[Response[IO]] = {
    for {
      authors <- authorService.getAll
      response <- Ok(authors.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

  private[http] def get(authorName: String): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      name <- AuthorName(authorName).liftTo[IO]
      maybeAuthor <- authorService.findByName(name)
    } yield maybeAuthor).flatMap(_.asResponse)
  }

}
