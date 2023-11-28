package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.http.MethodNotAllowedSupport
import net.yoshinorin.qualtet.syntax.*

class ContentTypeRoute[F[_]: Monad](
  contentTypeService: ContentTypeService[F]
) extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => this.get
    case GET -> Root / name => this.get(name)
    case OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  private[http] def get: IO[Response[IO]] = {
    for {
      allContentTypes <- contentTypeService.getAll
      response <- Ok(allContentTypes.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

  private[http] def get(name: String): IO[Response[IO]] = {
    (for {
      maybeContentType <- contentTypeService.findByName(name)
    } yield maybeContentType).flatMap(_.asResponse)
  }
}
