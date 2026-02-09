package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.Concurrent
import cats.implicits.*
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl

import net.yoshinorin.qualtet.ApplicationInfo
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.config.HttpSystemEndpointConfig
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class SystemRoute[F[_]: Concurrent](config: HttpSystemEndpointConfig)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    (r match {
      case request @ GET -> Root / "health" => this.health
      case request @ GET -> Root / "metadata" =>
        if config.metadata.enabled then this.metadata else NotFound()
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  // system/health
  private[http] def health: F[Response[F]] = {
    Ok()
  }

  // system/metadata
  private[http] def metadata: F[Response[F]] = {
    (for {
      response <- ApplicationInfo.asJson.asResponse[F](Ok)
    } yield response)
  }

}
