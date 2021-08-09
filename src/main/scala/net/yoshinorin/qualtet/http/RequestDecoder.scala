package net.yoshinorin.qualtet.http

import io.circe.Decoder
import net.yoshinorin.qualtet.domains.models.Fail.BadRequest
import net.yoshinorin.qualtet.domains.models.Fail
import org.slf4j.LoggerFactory

trait RequestDecoder {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def decode[T](string: String)(implicit d: Decoder[T]): Either[Fail, T] = {
    io.circe.parser.decode[T](string) match {
      case Right(v) =>
        Right(v)
      case Left(error) =>
        logger.error(error.getMessage)
        Left(BadRequest(error.getMessage))
    }
  }

}
