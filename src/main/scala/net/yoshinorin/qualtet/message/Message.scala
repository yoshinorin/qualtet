package net.yoshinorin.qualtet.message

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

final case class Message(
  message: String
)

object Message {
  given codecMessage: JsonValueCodec[Message] = JsonCodecMaker.make
}
