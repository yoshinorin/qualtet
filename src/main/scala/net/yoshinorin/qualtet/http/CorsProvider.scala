package net.yoshinorin.qualtet.http

import cats.effect.IO
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.Uri
import org.http4s.server.middleware.*
import org.http4s.headers.Origin
import org.typelevel.log4cats.LoggerFactory as Log4CatsLoggerFactory
import net.yoshinorin.qualtet.config.CorsConfig
import java.net.URI

class CorsProvider(
  corsConfig: CorsConfig
)(using logger: Log4CatsLoggerFactory[IO]) {

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

  def httpRouter(route: HttpRoutes[IO]) = policyWithAllowOrigin.httpRoutes(route)

  def httpApp(app: HttpApp[IO]) = policyWithAllowOrigin.httpApp(app)

}
