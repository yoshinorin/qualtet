package net.yoshinorin.qualtet.http.routes

import cats.effect._, cats.implicits._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server._
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
import org.http4s.server.Router

class ContentRoute(
  contentService: ContentService
) extends RequestDecoder {

  def post(payload: (ResponseAuthor, String)) = {
    val maybeContent = for {
      maybeContent <- IO(decode[RequestContent](payload._2))
    } yield maybeContent

    maybeContent.flatMap { c =>
      c match {
        case Left(f) => throw f
        case Right(c) =>
          contentService.createContentFromRequest(payload._1.name, c).flatMap { r =>
            Ok(c.asJson, `Content-Type`(MediaType.application.json))
          }
      }
    }
  }

  def delete(id: String) = {
    for {
      // TODO: `id.segments.last` is correct way?
      _ <- contentService.delete(ContentId(id))
      // TODO: logging
      response <- NoContent()
    } yield response
  }

  def get(path: String) = {
    for {
      // TODO: avoid to add slash to prefix and suffix
      // TODO: should be configurlize for append suffix or prefix
      contents <- contentService.findByPathWithMeta(Path(s"/${path}"))
      _ = println(path)
      // TODO: return `NotFound` if contents is None
      response <- Ok(contents.get.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
