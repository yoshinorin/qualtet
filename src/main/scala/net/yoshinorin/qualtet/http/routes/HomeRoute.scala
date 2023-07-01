package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.*
import org.http4s.dsl.io.*

class HomeRoute {

  // application root
  def get: IO[Response[IO]] = {
    Ok("Hello Qualtet!!")
  }

}
