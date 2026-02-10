package net.yoshinorin.qualtet

import cats.effect.Async
import cats.data.Kleisli
import org.http4s.{HttpApp, Request, Response}
import org.http4s.server.middleware.{Logger, RequestId, ResponseTiming}
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.log4cats.LoggerFactory as Log4CatsLoggerFactory
import net.yoshinorin.qualtet.infrastructure.telemetry.HttpTracing

class HttpAppBuilder[F[_]: Async](routes: Kleisli[F, Request[F], Response[F]], tracer: Option[Tracer[F]] = None)(using logger: Log4CatsLoggerFactory[F]) {

  def build: HttpApp[F] = {

    // NOTE: https://github.com/http4s/http4s/blob/v1.0.0-M39/server/shared/src/main/scala/org/http4s/server/middleware/RequestId.scala
    val withRequestIdHeader = RequestId(routes)

    // NOTE: https://github.com/http4s/http4s/blob/v1.0.0-M39/server/shared/src/main/scala/org/http4s/server/middleware/ResponseTiming.scala
    val withResponseTimingHeader = ResponseTiming(withRequestIdHeader)

    // Apply OpenTelemetry tracing middleware
    val withOtelMiddleware = tracer.fold(withResponseTimingHeader) { tracerInstance =>
      HttpTracing(tracerInstance)(withResponseTimingHeader)
    }

    val log = logger.getLogger
    Logger.httpApp[F](logHeaders = true, logBody = false, logAction = Some(msg => log.info(msg)))(withOtelMiddleware)
  }

}
