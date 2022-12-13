package net.yoshinorin.qualtet.http.routes

import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._

class ApiStatusRoute {

  // status
  def get = {
    Ok("{\"status\":\"operational\"}", `Content-Type`(MediaType.application.json))
  }

}
