package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.io.*

class HomeRoute {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root => this.get
    case request @ OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
    case request @ _ =>
      NotFound("Not found")
  }

  // application root
  private[http] def get: IO[Response[IO]] = {
    Ok("Hello Qualtet!!")
  }

}
