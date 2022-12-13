package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._

class ApiStatusRoute {

  // status
  def get: IO[Response[IO]]  = {
    Ok("{\"status\":\"operational\"}", `Content-Type`(MediaType.application.json))
  }

}
