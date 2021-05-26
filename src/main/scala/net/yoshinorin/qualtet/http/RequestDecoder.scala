package net.yoshinorin.qualtet.http

import io.circe.Decoder
import net.yoshinorin.qualtet.domains.models.Message

trait RequestDecoder {

  def decode[T](string: String)(implicit d: Decoder[T]): Either[Message, T] = {
    io.circe.parser.decode[T](string) match {
      case Right(v) => Right(v)
      case Left(error) =>
        // TODO: logging
        Left(Message(error.getMessage))
    }
  }

}
