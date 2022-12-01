package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.contents.{Content, ContentService, Path, RequestContent}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.contents.ResponseContent._
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.http.{AuthorizationProvider, RequestDecoder}
import net.yoshinorin.qualtet.syntax._

class ContentRoute(
  authorizationProvider: AuthorizationProvider,
  contentService: ContentService
) extends RequestDecoder {

  // contents
  def route: HttpRoutes[IO] =
    // authorizationProvider.authenticate(authedRoute) <+> nonAuthedRoute
    nonAuthRoute

  val authedRoute: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case request @ POST -> Root as payload => {
      val maybeContent = for {
        maybeContent <- IO(decode[RequestContent](payload._2))
      } yield maybeContent

      maybeContent.flatMap { c =>
        c match {
          case Left(f) => throw f
          case Right(c) => contentService.createContentFromRequest(payload._1.name, c).flatMap { r =>
            Ok(c.asJson, `Content-Type`(MediaType.application.json))
          }
        }
      }
    }
    // TODO: manually test
    case DELETE -> "TODO" /: id as payload => {
      for {
        // TODO: `id.segments.last` is correct way?
        _ <- contentService.delete(ContentId(id.segments.last.toString))
        // TODO: logging
        response <- NoContent()
      } yield response
    }
  }

  val nonAuthRoute = HttpRoutes.of[IO] {
    // need slash on the prefix and suffix.
    // example: /yyyy/mm/dd/content-name/
    case GET -> "TODO" /: path => {
      for {
        // TODO: avoid to add slash to prefix and suffix
        contents <- contentService.findByPathWithMeta(Path(s"/${path.toString()}/"))
        // TODO: return `NotFound` if contents is None
        response <- Ok(contents.get.asJson, `Content-Type`(MediaType.application.json))
      } yield response
    }
  }

}
