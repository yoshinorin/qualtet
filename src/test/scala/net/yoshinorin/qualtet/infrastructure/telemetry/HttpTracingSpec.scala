package net.yoshinorin.qualtet.infrastructure.telemetry

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.implicits.*
import org.typelevel.ci.*
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global
import cats.data.Kleisli

// testOnly net.yoshinorin.qualtet.infrastructure.telemetry.HttpTracingSpec
class HttpTracingSpec extends AnyWordSpec {

  val mockTracer: Tracer[IO] = Tracer.noop[IO]

  val simpleApp: HttpApp[IO] = Kleisli { _ =>
    IO(Response[IO](Status.Ok))
  }

  "HttpTracing" should {

    "create a tracing middleware that processes requests" in {
      val tracingMiddleware = HttpTracing(mockTracer)
      val tracedApp = tracingMiddleware(simpleApp)
      val client: Client[IO] = Client.fromHttpApp(tracedApp)

      client
        .run(Request(method = Method.GET, uri = uri"/test"))
        .use { response =>
          IO {
            assert(response.status === Status.Ok)
          }
        }
        .unsafeRunSync()
    }
  }

  "TextMapGetter[Headers]" should {

    "extract header values correctly" in {
      val headers = Headers(
        Header.Raw(ci"test-header", "test-value"),
        Header.Raw(ci"another-header", "another-value")
      )

      import HttpTracing.given

      assert(summon[TextMapGetter[Headers]].get(headers, "test-header").contains("test-value"))
      assert(summon[TextMapGetter[Headers]].get(headers, "another-header").contains("another-value"))
      assert(summon[TextMapGetter[Headers]].get(headers, "non-existent").isEmpty)
    }

    "return all header keys" in {
      val headers = Headers(
        Header.Raw(ci"header1", "value1"),
        Header.Raw(ci"header2", "value2")
      )

      import HttpTracing.given
      val keys = summon[TextMapGetter[Headers]].keys(headers).toSet

      assert(keys.contains("header1"))
      assert(keys.contains("header2"))
      assert(keys.size === 2)
    }
  }

}
