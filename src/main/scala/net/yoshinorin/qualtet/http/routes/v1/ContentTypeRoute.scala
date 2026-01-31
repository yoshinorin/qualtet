package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.effect.IO
import cats.Monad
import org.http4s.Request
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class ContentTypeRoute[F[_]: Monad @nowarn](
  contentTypeService: ContentTypeService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root => this.get
      case request @ GET -> Root / name => this.get(name)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].asResponse)
  }

  private[http] def get: IO[Response[IO]] = {
    for {
      allContentTypes <- contentTypeService.getAll
      response <- allContentTypes.asResponse(Ok)
    } yield response
  }

  private[http] def get(name: String): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      contentTypeName <- EitherT.fromEither[IO](ContentTypeName(name))
      maybeContentType <- EitherT.liftF(contentTypeService.findByName(contentTypeName))
    } yield maybeContentType).value.flatMap {
      case Right(contentType) => contentType.asResponse
      case Left(error: DomainError) => error.asResponse
    }
  }
}
