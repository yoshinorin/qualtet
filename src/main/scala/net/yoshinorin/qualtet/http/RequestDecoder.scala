package net.yoshinorin.qualtet.http

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.decoding.ConfiguredDecoder
import net.yoshinorin.qualtet.domains.models.Fail.BadRequest
import net.yoshinorin.qualtet.domains.models.Fail
import org.slf4j.LoggerFactory

trait RequestDecoder {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)
  implicit val circeCustomConfig: Configuration = Configuration.default.withDefaults

  def decode[T](string: String)(implicit d: ConfiguredDecoder[T]): Either[Fail, T] = {
    io.circe.parser.decode[T](string) match {
      case Right(v) =>
        Right(v)
      case Left(error) =>
        logger.error(error.getMessage)
        Left(BadRequest(error.getMessage))
    }
  }

}
