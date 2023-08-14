package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.http.MethodNotAllowedSupport

class SystemRoute extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" => this.get
    case OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  // system
  private[http] def get: IO[Response[IO]] = {
    Ok()
  }

}
