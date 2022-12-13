package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._

class HomeRoute {

  // application root
  def get: IO[Response[IO]] = {
    Ok("Hello Qualtet!!")
  }

}
