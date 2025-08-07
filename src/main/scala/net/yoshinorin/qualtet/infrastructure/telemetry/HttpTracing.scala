package net.yoshinorin.qualtet.infrastructure.telemetry

import cats.data.Kleisli
import cats.effect.IO
import org.http4s.{Headers, HttpApp}
import org.typelevel.ci.CIString
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.trace.Tracer

object HttpTracing {

  given TextMapGetter[Headers] with
    def get(headers: Headers, key: String): Option[String] = headers.get(CIString(key)).map(_.head.value)
    def keys(headers: Headers): Iterable[String] = headers.headers.map(_.name.toString)

  def apply(tracer: Tracer[IO]): HttpApp[IO] => HttpApp[IO] = { httpApp =>
    Kleisli { request =>
      tracer.joinOrRoot(request.headers) {
        tracer
          .spanBuilder(s"${request.method} ${request.uri.path}")
          .addAttribute(Attribute("http.method", request.method.name))
          .addAttribute(Attribute("http.url", request.uri.toString))
          .addAttribute(Attribute("http.scheme", request.uri.scheme.fold("http")(_.value.toString)))
          .addAttribute(Attribute("http.host", request.uri.host.fold("localhost")(_.value.toString)))
          .build
          .use { span =>
            httpApp(request).flatMap { response =>
              span
                .addAttribute(Attribute("http.status_code", response.status.code.toLong))
                .as(response)
            }
          }
      }
    }
  }

}
