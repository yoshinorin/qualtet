package net.yoshinorin.qualtet

import cats.effect.IO
import cats.data.Kleisli
import org.http4s.{HttpApp, Request, Response}
import org.http4s.server.middleware.{Logger, RequestId, ResponseTiming}
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.log4cats.LoggerFactory as Log4CatsLoggerFactory
import net.yoshinorin.qualtet.infrastructure.telemetry.HttpTracing

class HttpAppBuilder(routes: Kleisli[IO, Request[IO], Response[IO]], tracer: Option[Tracer[IO]] = None)(using logger: Log4CatsLoggerFactory[IO]) {

  def build: HttpApp[IO] = {

    // NOTE: https://github.com/http4s/http4s/blob/v1.0.0-M39/server/shared/src/main/scala/org/http4s/server/middleware/RequestId.scala
    val withRequestIdHeader = RequestId(routes)

    // NOTE: https://github.com/http4s/http4s/blob/v1.0.0-M39/server/shared/src/main/scala/org/http4s/server/middleware/ResponseTiming.scala
    val withResponseTimingHeader = ResponseTiming(withRequestIdHeader)

    // Apply OpenTelemetry tracing middleware
    val withOtelMiddleware = tracer.fold(withResponseTimingHeader) { tracerInstance =>
      HttpTracing(tracerInstance)(withResponseTimingHeader)
    }

    // TODO: filter & format log
    Logger.httpApp(logHeaders = true, logBody = false)(withOtelMiddleware)
  }

}
