package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.Request
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.http.MethodNotAllowedSupport

class AuthorRoute[F[_]: Monad](
  authorService: AuthorService[F]
) extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { r =>
    implicit val x = r
    (r match {
      case request @ GET -> Root => this.get
      case request @ GET -> Root / authorName => this.get(authorName)
      case request @ OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
      case request @ _ =>
        methodNotAllowed(request, Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  // authors
  private[http] def get: IO[Response[IO]] = {
    for {
      authors <- authorService.getAll
      response <- Ok(authors.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

  private[http] def get(authorName: String)(implicit r: Request[IO]): IO[Response[IO]] = {
    (for {
      maybeAuthor <- authorService.findByName(AuthorName(authorName))
    } yield maybeAuthor).flatMap(_.asResponse)
  }

}
