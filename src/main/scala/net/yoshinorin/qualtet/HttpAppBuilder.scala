package net.yoshinorin.qualtet

import cats.effect.IO
import cats.data.Kleisli
import org.http4s.{HttpApp, Request, Response}
import org.http4s.server.middleware.{RequestId, ResponseTiming, Logger}

class HttpAppBuilder(routes: Kleisli[IO, Request[IO], Response[IO]]) {

  def build: HttpApp[IO] = {

    // NOTE: https://github.com/http4s/http4s/blob/v1.0.0-M39/server/shared/src/main/scala/org/http4s/server/middleware/RequestId.scala
    val withRequestIdHeader = RequestId(routes)

    // NOTE: https://github.com/http4s/http4s/blob/v1.0.0-M39/server/shared/src/main/scala/org/http4s/server/middleware/ResponseTiming.scala
    val withResponseTimingHeader = ResponseTiming(withRequestIdHeader)

    // TODO: filter & format log
    Logger.httpApp(logHeaders = true, logBody = false)(withResponseTimingHeader)
  }

}
