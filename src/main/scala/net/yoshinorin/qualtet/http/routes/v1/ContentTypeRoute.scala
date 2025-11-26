package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.implicits.*
import cats.Monad
import org.http4s.Request
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class ContentTypeRoute[F[_]: Monad](
  contentTypeService: ContentTypeService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ GET -> Root / name =>
        ContentTypeName(name).liftTo[IO].flatMap(contentTypeName => this.get(contentTypeName))
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def get: IO[Response[IO]] = {
    for {
      allContentTypes <- contentTypeService.getAll
      response <- Ok(allContentTypes.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

  private[http] def get(name: ContentTypeName): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      maybeContentType <- contentTypeService.findByName(name)
    } yield maybeContentType).flatMap(_.asResponse)
  }
}
