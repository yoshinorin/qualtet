package net.yoshinorin.qualtet.domains

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

opaque type Path = String
object Path extends ValueExtender[Path] {
  given codecPath: JsonValueCodec[Path] = JsonCodecMaker.make

  def apply(value: String): Path = {
    // TODO: check valid url https://www.ietf.org/rfc/rfc3986.txt
    if (!value.startsWith("/")) {
      s"/${value}"
    } else {
      value
    }
  }
}
