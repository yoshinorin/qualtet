package net.yoshinorin.qualtet.http

import io.circe.Decoder
import net.yoshinorin.qualtet.domains.models.Fail.{BadRequest, InternalServerError}
import net.yoshinorin.qualtet.domains.models.Fail
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

trait RequestDecoder {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def decode[T](string: String)(implicit d: Decoder[T]): Either[Fail, T] = {
    try {
      io.circe.parser.decode[T](string) match {
        case Right(v) =>
          Right(v)
        case Left(error) =>
          logger.error(error.getMessage)
          Left(BadRequest(error.getMessage))
      }
    } catch {
      case NonFatal(t) =>
        logger.error(t.getMessage)
        t match {
          case t: Fail => Left(t)
          case _ => Left(InternalServerError())
        }
    }
  }

}
