package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.Request
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService

import net.yoshinorin.qualtet.syntax.*

class ContentTypeRoute[F[_]: Monad](
  contentTypeService: ContentTypeService[F]
) {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ GET -> Root / name => this.get(name)
      case request @ OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def get: IO[Response[IO]] = {
    for {
      allContentTypes <- contentTypeService.getAll
      response <- Ok(allContentTypes.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

  private[http] def get(name: String): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      maybeContentType <- contentTypeService.findByName(name)
    } yield maybeContentType).flatMap(_.asResponse)
  }
}
