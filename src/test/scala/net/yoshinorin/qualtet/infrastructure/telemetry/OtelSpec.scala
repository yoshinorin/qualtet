package net.yoshinorin.qualtet.infrastructure.telemetry

import cats.effect.unsafe.IORuntime
import net.yoshinorin.qualtet.config.{OtelConfig, OtelExporterConfig, OtelServiceConfig}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.infrastructure.telemetry.OtelSpec
class OtelSpec extends AnyWordSpec {

  private val runtime: IORuntime = IORuntime.global

  private val baseConfig = OtelConfig(
    enabled = Some(true),
    service = OtelServiceConfig(
      name = Some("test-service"),
      namespace = Some("test-namespace")
    ),
    exporter = OtelExporterConfig(
      endpoint = Some("http://localhost:4317"),
      headers = None,
      protocol = None
    ),
    propagator = Some("tracecontext")
  )

  "Otel.configureSystemProperties" should {

    "return properties with all values when config has all optional values" in {
      val config = baseConfig.copy(propagator = Some("tracecontext,baggage"))

      val properties = Otel.configureSystemProperties(config)

      assert(properties("otel.java.global-autoconfigure.enabled") === "true")
      assert(properties("otel.service.version") === "v2.21.0") // BuildInfo.version
      assert(properties("otel.service.name") === "test-service")
      assert(properties("otel.service.namespace") === "test-namespace")
      assert(properties("otel.exporter.otlp.endpoint") === "http://localhost:4317")
      assert(properties("otel.propagators") === "tracecontext,baggage")
      assert(properties("otel.resource.attributes") === "service.name=test-service,service.version=v2.21.0,service.namespace=test-namespace")
      assert(properties("otel.exporter.otlp.protocol") === "http/protobuf")
    }

    "return properties with defaults when config has no optional values" in {
      val config = baseConfig.copy(
        service = OtelServiceConfig(name = None, namespace = None),
        exporter = OtelExporterConfig(endpoint = None, headers = None, protocol = None),
        propagator = None
      )

      val properties = Otel.configureSystemProperties(config)

      assert(properties("otel.java.global-autoconfigure.enabled") === "true")
      assert(properties("otel.service.version") === "v2.21.0")
      assert(properties("otel.propagators") === "tracecontext") // default
      assert(properties("otel.resource.attributes") === "service.name=qualtet,service.version=v2.21.0")
      assert(properties("otel.exporter.otlp.protocol") === "http/protobuf")
      assert(!properties.contains("otel.service.name"))
      assert(!properties.contains("otel.service.namespace"))
      assert(!properties.contains("otel.exporter.otlp.endpoint"))
    }

    "return properties with mixed optional and default values" in {
      val config = baseConfig.copy(
        service = baseConfig.service.copy(name = Some("mixed-service"), namespace = None),
        exporter = OtelExporterConfig(endpoint = None, headers = None, protocol = None),
        propagator = Some("b3")
      )

      val properties = Otel.configureSystemProperties(config)

      assert(properties("otel.java.global-autoconfigure.enabled") === "true")
      assert(properties("otel.service.version") === "v2.21.0")
      assert(properties("otel.service.name") === "mixed-service")
      assert(properties("otel.propagators") === "b3")
      assert(properties("otel.exporter.otlp.protocol") === "http/protobuf")
      assert(!properties.contains("otel.service.namespace"))
      assert(!properties.contains("otel.exporter.otlp.endpoint"))
    }

    "return properties with headers when exporter.headers is provided" in {
      val config = baseConfig.copy(
        exporter = baseConfig.exporter.copy(headers = Some("Authorization=Bearer token123,X-Custom=value"))
      )

      val properties = Otel.configureSystemProperties(config)

      assert(properties("otel.exporter.otlp.headers") === "Authorization=Bearer token123,X-Custom=value")
    }

    "not include headers property when exporter.headers is None" in {
      val config = baseConfig.copy(
        exporter = baseConfig.exporter.copy(headers = None)
      )

      val properties = Otel.configureSystemProperties(config)

      assert(!properties.contains("otel.exporter.otlp.headers"))
    }

    "return consistent property count for same config type" in {
      val minimalConfig = baseConfig.copy(
        service = OtelServiceConfig(name = None, namespace = None),
        exporter = OtelExporterConfig(endpoint = None, headers = None, protocol = None),
        propagator = None
      )
      val config1 = minimalConfig.copy(enabled = Some(true))
      val config2 = minimalConfig.copy(enabled = Some(false))

      val properties1 = Otel.configureSystemProperties(config1)
      val properties2 = Otel.configureSystemProperties(config2)

      // Both should have the same required properties
      assert(properties1.contains("otel.java.global-autoconfigure.enabled"))
      assert(properties1.contains("otel.service.version"))
      assert(properties1.contains("otel.propagators"))
      assert(properties1.contains("otel.exporter.otlp.protocol"))

      assert(properties2.contains("otel.java.global-autoconfigure.enabled"))
      assert(properties2.contains("otel.service.version"))
      assert(properties2.contains("otel.propagators"))
      assert(properties2.contains("otel.exporter.otlp.protocol"))
    }

    "return properties with grpc protocol when configured" in {
      val config = baseConfig.copy(
        exporter = baseConfig.exporter.copy(protocol = Some("grpc"))
      )

      val properties = Otel.configureSystemProperties(config)

      assert(properties("otel.exporter.otlp.protocol") === "grpc")
    }

    "return properties with default protocol when protocol is None" in {
      val config = baseConfig.copy(
        exporter = baseConfig.exporter.copy(protocol = None)
      )

      val properties = Otel.configureSystemProperties(config)

      assert(properties("otel.exporter.otlp.protocol") === "http/protobuf")
    }
  }

  "Otel.buildResourceAttributes" should {

    "return all resource attributes when config has all values" in {
      val resourceAttributes = Otel.buildResourceAttributes(baseConfig)

      assert(resourceAttributes === "service.name=test-service,service.version=v2.21.0,service.namespace=test-namespace")
    }

    "return minimal resource attributes when config has no optional values" in {
      val config = baseConfig.copy(
        service = OtelServiceConfig(name = None, namespace = None)
      )
      val resourceAttributes = Otel.buildResourceAttributes(config)

      assert(resourceAttributes === "service.name=qualtet,service.version=v2.21.0")
    }

    "return resource attributes with only service name when namespace is None" in {
      val config = baseConfig.copy(
        service = baseConfig.service.copy(namespace = None)
      )
      val resourceAttributes = Otel.buildResourceAttributes(config)

      assert(resourceAttributes === "service.name=test-service,service.version=v2.21.0")
    }

    "return resource attributes with custom service name" in {
      val config = baseConfig.copy(
        service = baseConfig.service.copy(name = Some("custom-service"))
      )
      val resourceAttributes = Otel.buildResourceAttributes(config)

      assert(resourceAttributes === "service.name=custom-service,service.version=v2.21.0,service.namespace=test-namespace")
    }
  }

  "Otel.initialize" should {

    // NOTE: Testing otelConfig.enabled = true case is not implemented because:
    // - It modifies global system properties which can affect other tests
    // - It requires OpenTelemetry SDK initialization which is complex and may fail in test environment
    // - System property changes are not easily isolated between test cases

    "return None when otelConfig.enabled is false" in {
      val config = baseConfig.copy(enabled = Some(false))
      assert(Otel.initialize(runtime, config).isEmpty)
    }

    "return None when otelConfig.enabled is None (defaults to false)" in {
      val config = baseConfig.copy(enabled = None)
      assert(Otel.initialize(runtime, config).isEmpty)
    }

    "return None when otelConfig.enabled is true but exporter.endpoint is None" in {
      val config = baseConfig.copy(exporter = OtelExporterConfig(endpoint = None, headers = None, protocol = None))
      assert(Otel.initialize(runtime, config).isEmpty)
    }

    "return None when otelConfig.enabled is true but exporter.endpoint is empty string" in {
      val config = baseConfig.copy(exporter = OtelExporterConfig(endpoint = Some(""), headers = None, protocol = None))
      assert(Otel.initialize(runtime, config).isEmpty)
    }

    "return None when otelConfig.enabled is true but exporter.endpoint is whitespace only" in {
      val config = baseConfig.copy(exporter = OtelExporterConfig(endpoint = Some("   "), headers = None, protocol = None))
      assert(Otel.initialize(runtime, config).isEmpty)
    }
  }

}
