package net.yoshinorin.qualtet.http

import cats.effect.IO
import cats.data.Kleisli
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.Uri
import org.http4s.{Request, Response}
import org.http4s.server.middleware.*
import org.http4s.headers.Origin
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.config.CorsConfig
import java.net.URI

import scala.util.control.NonFatal

class CorsProvider(
  corsConfig: CorsConfig
) {
  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private[http] val origins: Set[Origin.Host] = {
    corsConfig.allowOrigins
      .map(o => {
        try {
          val u = new URI(o).toURL
          val protocol = if (u.getProtocol().startsWith("https")) Uri.Scheme.https else Uri.Scheme.http
          Origin.Host(protocol, Uri.RegName(u.getHost()), Some(u.getPort()))
        } catch {
          case NonFatal(t) =>
            logger.error(s"invalid allow-origin config: ${o}")
            logger.error(t.getMessage())
            Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), None)
        }
      })
      .toSet
  }

  def httpRouter(route: HttpRoutes[IO]) = CORS.policy.withAllowOriginHost(origins).httpRoutes(route)

  def httpApp(app: HttpApp[IO]) = CORS.policy.withAllowOriginHost(origins).httpApp(app)

}
