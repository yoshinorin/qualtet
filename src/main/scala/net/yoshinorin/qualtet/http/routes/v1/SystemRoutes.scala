package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.http.MethodNotAllowedSupport
import net.yoshinorin.qualtet.ApplicationInfo
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.config.HttpSystemEndpointConfig

class SystemRoute(config: HttpSystemEndpointConfig) extends MethodNotAllowedSupport[IO] {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root / "health" => this.health
      case request @ GET -> Root / "metadata" =>
        if config.metadata.enabled then this.metadata else NotFound()
      case request @ OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
      case request @ _ =>
        methodNotAllowed(request, Allow(Set(GET)))
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
