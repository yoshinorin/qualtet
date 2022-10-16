package net.yoshinorin.qualtet.http.routes

import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import cats.effect.IO
import cats.data.Kleisli

class HomeRoute {

  def route: HttpRoutes[IO] = HttpRoutes
    .of[IO] { case GET -> Root =>
      Ok("Hello Qualtet!!")
    }

}
