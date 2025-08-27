package net.yoshinorin.qualtet.infrastructure.telemetry

import cats.effect.{IO, Resource, Sync}
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.runtimemetrics.java17.*
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.Tracer
import net.yoshinorin.qualtet.config.OtelConfig
import net.yoshinorin.qualtet.buildinfo.BuildInfo

object Otel {

  def initialize(otelConfig: OtelConfig): Option[Resource[IO, Tracer[IO]]] = {
    val isEnabled = otelConfig.enabled.getOrElse(false)
    val hasValidEndpoint = otelConfig.exporter.endpoint.exists(_.trim.nonEmpty)

    Option.when(isEnabled && hasValidEndpoint) {
      val properties = configureSystemProperties(otelConfig)
      properties.foreach { case (key, value) =>
        System.setProperty(key, value)
      }
      createTracerResource(otelConfig)
    }
  }

  private[telemetry] def configureSystemProperties(otelConfig: OtelConfig): Map[String, String] = {
    val baseProperties = Map(
      "otel.java.global-autoconfigure.enabled" -> "true",
      "otel.service.version" -> BuildInfo.version,
      "otel.propagators" -> otelConfig.propagator.getOrElse("tracecontext"),
      "otel.resource.attributes" -> buildResourceAttributes(otelConfig),
      "otel.exporter.otlp.protocol" -> "http/protobuf"
    )

    val optionalProperties = List(
      otelConfig.service.name.map("otel.service.name" -> _),
      otelConfig.service.namespace.map("otel.service.namespace" -> _),
      otelConfig.exporter.endpoint.map("otel.exporter.otlp.endpoint" -> _),
      otelConfig.exporter.headers.map("otel.exporter.otlp.headers" -> _)
    ).flatten.toMap

    baseProperties ++ optionalProperties
  }

  private[telemetry] def buildResourceAttributes(otelConfig: OtelConfig): String = {
    val attributes = List(
      Some(s"service.name=${otelConfig.service.name.getOrElse("qualtet")}"),
      Some(s"service.version=${BuildInfo.version}"),
      otelConfig.service.namespace.map(ns => s"service.namespace=$ns")
    ).flatten

    attributes.mkString(",")
  }

  private def createTracerResource(otelConfig: OtelConfig): Resource[IO, Tracer[IO]] = {
    val serviceName = otelConfig.service.name.getOrElse("qualtet")
    OtelJava
      .autoConfigured[IO]()
      .flatTap(otel4s => registerRuntimeMetrics(otel4s.underlying))
      .flatTap(otel4s => registerLogAppender(otel4s.underlying))
      .flatMap { otel =>
        Resource.make(otel.tracerProvider.get(serviceName))(_ => IO.pure(()))
      }
  }

  private def registerRuntimeMetrics[F[_]: Sync](
    openTelemetry: OpenTelemetry
  ): Resource[F, Unit] = {
    val acquire = Sync[F].delay(RuntimeMetrics.create(openTelemetry))
    Resource.fromAutoCloseable(acquire).void
  }

  private def registerLogAppender[F[_]: Sync](
    openTelemetry: OpenTelemetry
  ): Resource[F, Unit] = {
    Resource.eval(Sync[F].delay(OpenTelemetryAppender.install(openTelemetry)))
  }

}
