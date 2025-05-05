package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*

import net.yoshinorin.qualtet.ApplicationInfo
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.config.HttpSystemEndpointConfig
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class SystemRoute(config: HttpSystemEndpointConfig)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root / "health" => this.health
      case request @ GET -> Root / "metadata" =>
        if config.metadata.enabled then this.metadata else NotFound()
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  // system/health
  private[http] def health: IO[Response[IO]] = {
    Ok()
  }

  // system/metadata
  private[http] def metadata: IO[Response[IO]] = {
    (for {
      a <- IO(ApplicationInfo.asJson)
      response <- Ok(a, `Content-Type`(MediaType.application.json))
    } yield response)
  }

}
