package net.yoshinorin.qualtet.infrastructure.telemetry

import cats.effect.{IO, Resource}
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.Tracer

object Otel {

  def initialize: Resource[IO, Tracer[IO]] = {
    OtelJava.autoConfigured[IO]().flatMap { otel =>
      Resource.make(otel.tracerProvider.get("net.yoshinorin.qualtet"))(_ => IO.pure(()))
    }
  }

}
