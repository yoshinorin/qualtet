package net.yoshinorin.qualtet.http.routes

import cats.effect._
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.contents.{ContentService, Path, RequestContent}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.contents.ResponseContent._
import net.yoshinorin.qualtet.http.RequestDecoder
import net.yoshinorin.qualtet.syntax._

class ContentRoute(
  contentService: ContentService
) extends RequestDecoder {

  def post(payload: (ResponseAuthor, String)): IO[Response[IO]] = {
    val maybeContent = for {
      maybeContent <- IO(decode[RequestContent](payload._2))
    } yield maybeContent

    maybeContent.flatMap { c =>
      c match {
        case Left(f) => throw f
        case Right(c) =>
          contentService.createContentFromRequest(payload._1.name, c).flatMap { _ =>
            Ok(c.asJson, `Content-Type`(MediaType.application.json))
          }
      }
    }
  }

  def delete(id: String): IO[Response[IO]] = {
    for {
      _ <- contentService.delete(ContentId(id))
      // TODO: logging
      response <- NoContent()
    } yield response
  }

  def get(path: String): IO[Response[IO]] = {
    for {
      // TODO: avoid to add slash to prefix and suffix
      // TODO: should be configurlize for append suffix or prefix
      contents <- contentService.findByPathWithMeta(Path(s"/${path}"))
      // TODO: return `NotFound` if contents is None
      response <- Ok(contents.get.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
