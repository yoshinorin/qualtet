package net.yoshinorin.qualtet.http

import net.yoshinorin.qualtet.config.CorsConfig

import cats.effect.Concurrent
import org.http4s.{HttpApp, HttpRoutes, Uri}
import org.http4s.headers.Origin
import org.http4s.server.middleware.*
import org.typelevel.log4cats.LoggerFactory as Log4CatsLoggerFactory
import java.net.URI

class CorsProvider[F[_]: Concurrent](
  corsConfig: CorsConfig
)(using logger: Log4CatsLoggerFactory[F]) {

  private[http] val origins: Set[Origin.Host] = {
    // NOTE: throw `java.net.MalformedURLException` if configs contains invalid URL.
    corsConfig.allowOrigins
      .map(o => {
        val u = new URI(o).toURL
        val protocol = if (u.getProtocol().startsWith("https")) Uri.Scheme.https else Uri.Scheme.http
        Origin.Host(protocol, Uri.RegName(u.getHost()), Some(u.getPort()))
      })
      .toSet
  }

  private[http] val policyWithAllowOrigin: CORSPolicy = {
    if (origins.isEmpty) CORS.policy.withAllowOriginAll else CORS.policy.withAllowOriginHost(origins)
  }

  def httpRouter(route: HttpRoutes[F]) = policyWithAllowOrigin.httpRoutes(route)

  def httpApp(app: HttpApp[F]) = policyWithAllowOrigin.httpApp(app)

}
