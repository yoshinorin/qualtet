package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.syntax._

class ContentTypeRoute(
  contentTypeService: ContentTypeService
) {

  def get: IO[Response[IO]] = {
    for {
      allContentTypes <- contentTypeService.getAll
      response <- Ok(allContentTypes.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

  def get(name: String): IO[Response[IO]] = {
    (for {
      maybeContentType <- contentTypeService.findByName(name)
    } yield maybeContentType).flatMap { mct =>
      mct match {
        case Some(contentType) => Ok(contentType.asJson, `Content-Type`(MediaType.application.json))
        // TODO: return as JSON format
        case None => NotFound("Not Found")
      }
    }
  }
}
