package net.yoshinorin.qualtet.http

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.decoding.ConfiguredDecoder
import net.yoshinorin.qualtet.domains.models.Message

trait RequestDecoder {

  implicit val circeCustomConfig: Configuration = Configuration.default.withDefaults

  def decode[T](string: String)(implicit d: ConfiguredDecoder[T]): Either[Message, T] = {
    io.circe.parser.decode[T](string) match {
      case Right(v) => Right(v)
      case Left(error) =>
        // TODO: logging
        Left(Message(error.getMessage))
    }
  }

}
