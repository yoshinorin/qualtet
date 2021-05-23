package net.yoshinorin.qualtet.domains.models

import io.circe.Encoder
import io.circe.generic.semiauto._

final case class Message(
  message: String
)

object Message {
  implicit val encodeMessage: Encoder[Message] = deriveEncoder[Message]
}
