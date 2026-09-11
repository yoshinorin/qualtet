package net.yoshinorin.qualtet.http.routes.v1

import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.syntax.*

import cats.Monad
import cats.effect.Concurrent
import cats.implicits.*
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Allow
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import scala.annotation.nowarn

class AuthorRoute[F[_]: Concurrent, G[_]: Monad @nowarn](
  authorService: AuthorService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ GET -> Root / authorName => this.get(authorName)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  // authors
  private[http] def get: F[Response[F]] = {
    for {
      authors <- authorService.getAll
      response <- authors.asResponse(Ok)
    } yield response
  }

  private[http] def get(authorName: String): Request[F] ?=> F[Response[F]] = {
    AuthorName(authorName) match {
      case Right(name) =>
        authorService.findByName(name).flatMap(_.asResponse)
      case Left(error) =>
        error.asResponse
    }
  }

}
