package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s.*
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor.*
import net.yoshinorin.qualtet.syntax.*

class AuthorRoute[M[_]: Monad](
  authorService: AuthorService[M]
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
