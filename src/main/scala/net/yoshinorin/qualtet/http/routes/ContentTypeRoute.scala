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
    for {
      maybeContent <- contentTypeService.findByName(name)
      // TODO: return 404 if content is None.
      response <- Ok(maybeContent.get.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }
}
