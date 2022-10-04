package net.yoshinorin.qualtet.http.routes

import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import cats.effect.IO
import cats.data.Kleisli

class ApiStatusRoute {

  // TODO: pathEndOrSingleSlash
  def route: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes
    .of[IO] { case GET -> Root / "status" =>
      Ok("{\"status\":\"operational\"}", `Content-Type`(MediaType.application.json))
    }
    .orNotFound

}
