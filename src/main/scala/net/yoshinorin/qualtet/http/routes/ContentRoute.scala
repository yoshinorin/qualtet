package net.yoshinorin.qualtet.http.routes

import cats.effect._
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.contents.{ContentService, Path, RequestContent}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.contents.ResponseContent._
import net.yoshinorin.qualtet.http.RequestDecoder
import net.yoshinorin.qualtet.syntax._

class ContentRoute(
  contentService: ContentService
) extends RequestDecoder {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def post(payload: (ResponseAuthor, String)): IO[Response[IO]] = {
    val maybeContent = for {
      maybeContent <- IO(decode[RequestContent](payload._2))
    } yield maybeContent

    maybeContent.flatMap { c =>
      c match {
        case Left(f) => throw f
        case Right(c) =>
          contentService.createContentFromRequest(payload._1.name, c).flatMap { _ =>
            Created(c.asJson, `Content-Type`(MediaType.application.json))
          }
      }
    }.handleErrorWith(_.asResponse)
  }

  def delete(id: String): IO[Response[IO]] = {
    (for {
      _ <- contentService.delete(ContentId(id))
      _ = logger.info(s"deleted content: ${id}")
      response <- NoContent()
    } yield response).handleErrorWith(_.asResponse)
  }

  def get(path: String): IO[Response[IO]] = {
    (for {
      // TODO: should be configurlize for append suffix or prefix
      maybeContent <- contentService.findByPathWithMeta(Path(s"/${path}"))
    } yield maybeContent)
      .flatMap(_.asResponse)
      .handleErrorWith(_.asResponse)
  }

}
