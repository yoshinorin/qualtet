package net.yoshinorin.qualtet.http

import cats.effect.IO
import org.slf4j.LoggerFactory
import org.http4s.{Request, Response}
import org.http4s.headers.Allow
import org.http4s.dsl.io.*

trait MethodNotAllowedSupport {
  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private[http] def methodNotAllowed(request: Request[IO], allow: Allow): IO[Response[IO]] = {
    logger.error(s"method not allowed: ${request}")
    MethodNotAllowed(allow)
  }

}
