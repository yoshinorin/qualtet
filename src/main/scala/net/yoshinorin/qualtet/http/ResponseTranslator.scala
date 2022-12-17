package net.yoshinorin.qualtet.http

import cats.effect.IO
import org.http4s.dsl.io._
import org.http4s.Response
import org.http4s.Challenge
import org.http4s.headers.`WWW-Authenticate`
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.message.Message
import net.yoshinorin.qualtet.syntax._
import org.slf4j.LoggerFactory

object ResponseTranslator {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private[this] def failToResponse(f: Fail): IO[Response[IO]] = {
    f match {
      case Fail.NotFound(message) => NotFound(message)
      case Fail.Unauthorized(message) => Unauthorized(`WWW-Authenticate`(Challenge("Bearer", "Unauthorized")))
      case Fail.UnprocessableEntity(message) => UnprocessableEntity(message)
      case Fail.BadRequest(message) => BadRequest(message)
      case Fail.Forbidden(message) => Forbidden(message)
      case Fail.InternalServerError(message) => InternalServerError(message)
    }
  }

  def toFailureResponse(e: Throwable): IO[Response[IO]] = {
    logger.error(e.getMessage)
    e match {
      case f: Fail => this.failToResponse(f)
      case _ => InternalServerError("Internal Server Error")
    }
  }

}
