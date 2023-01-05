package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor._
import net.yoshinorin.qualtet.syntax._

class AuthorRoute[F[_]: Monad](
  authorService: AuthorService[F]
) {

  // authors
  def get: IO[Response[IO]] = {
    for {
      authors <- authorService.getAll
      response <- Ok(authors.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

  def get(authorName: String): IO[Response[IO]] = {
    (for {
      maybeAuthor <- authorService.findByName(AuthorName(authorName))
    } yield maybeAuthor).flatMap(_.asResponse)
  }

}
