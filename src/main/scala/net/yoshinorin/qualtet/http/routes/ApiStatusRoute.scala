package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.http.MethodNotAllowedSupport

@deprecated("This API will be removed in version `2.10`. Please use `SystemRoute` instead of this.")
class ApiStatusRoute extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => this.get
    case OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  // status
  private[http] def get: IO[Response[IO]] = {
    Ok("{\"status\":\"operational\"}", `Content-Type`(MediaType.application.json))
  }

}
