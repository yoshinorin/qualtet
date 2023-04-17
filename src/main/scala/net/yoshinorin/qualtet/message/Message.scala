package net.yoshinorin.qualtet.message

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

final case class Message(
  message: String
)

object Message {
  given codecMessage: JsonValueCodec[Message] = JsonCodecMaker.make
}
