package net.yoshinorin.qualtet.http.routes

import cats.effect.Concurrent
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl

class HomeRoute[F[_]: Concurrent] {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  private[http] def index: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root => this.get
    case request @ OPTIONS -> Root => NoContent()
    case request @ _ =>
      NotFound("Not found")
  }

  // application root
  private[http] def get: F[Response[F]] = {
    Ok("Hello Qualtet!!")
  }

}
