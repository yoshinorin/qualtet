package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor._
import net.yoshinorin.qualtet.http.ResponseHandler
import net.yoshinorin.qualtet.syntax._

class AuthorRoute(
  authorService: AuthorService
) extends ResponseHandler {

  // authors
  def get: IO[Response[IO]] = {
    for {
      authors <- authorService.getAll
      response <- Ok(authors.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

  def get(authorName: String): IO[Response[IO]] = {
    val maybeAuthor = for {
      maybeAuthor <- authorService.findByName(AuthorName(authorName))
    } yield maybeAuthor
    maybeAuthor.flatMap { author =>
      author match {
        case Some(author) => Ok(author.asJson, `Content-Type`(MediaType.application.json))
        case None => NotFound("Not Found")
      }
    }
  }

}
