package net.yoshinorin.qualtet.http

import cats.Monad
import org.slf4j.LoggerFactory
import org.http4s.{Request, Response}
import org.http4s.headers.Allow
import org.http4s.dsl.io.*

trait MethodNotAllowedSupport[F[_]: Monad] {
  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private[http] def methodNotAllowed(request: Request[F], allow: Allow) = {
    logger.error(s"method not allowed: ${request}")
    MethodNotAllowed(allow)
  }

}
