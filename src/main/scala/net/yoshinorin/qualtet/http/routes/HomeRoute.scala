package net.yoshinorin.qualtet.http.routes

import org.http4s.dsl.io._

class HomeRoute {

  // application root
  def get = {
    Ok("Hello Qualtet!!")
  }

}
