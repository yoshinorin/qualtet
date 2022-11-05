package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.data.OptionT
import org.http4s.HttpRoutes
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
  def route: HttpRoutes[IO] = HttpRoutes[IO] {
    {
      case GET -> Root =>
        for {
          authors <- OptionT.liftF(authorService.getAll)
          response <- OptionT.liftF(Ok(authors.asJson, `Content-Type`(MediaType.application.json)))
        } yield response
      // TODO: refactor
      case GET -> Root / authorName =>
        val maybeAuthor = for {
          maybeAuthor <- authorService.findByName(AuthorName(authorName))
        } yield maybeAuthor
        OptionT.liftF(maybeAuthor.flatMap { author =>
          author match {
            case Some(author) => Ok(author.asJson, `Content-Type`(MediaType.application.json))
            case None => NotFound("Not Found")
          }
        })
    }
  }

}
