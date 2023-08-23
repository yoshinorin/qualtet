package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.http.MethodNotAllowedSupport
import net.yoshinorin.qualtet.ApplicationInfo
import net.yoshinorin.qualtet.buildinfo.BuildInfo
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.config.HttpSystemEndpointConfig

class SystemRoute(config: HttpSystemEndpointConfig) extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" => this.health
    case GET -> Root / "metadata" =>
      if config.metadata.enabled then this.metadata else NotFound()
    case OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
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
    } yield response).handleErrorWith(_.logWithStackTrace.andResponse)
  }

}
