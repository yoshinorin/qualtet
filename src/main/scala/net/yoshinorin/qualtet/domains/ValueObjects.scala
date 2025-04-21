package net.yoshinorin.qualtet.domains

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.errors.{InvalidPath, UnexpectedException}
import java.net.{URI, URISyntaxException}

opaque type Path = String
object Path extends ValueExtender[Path] {
  given codecPath: JsonValueCodec[Path] = JsonCodecMaker.make

  def apply(value: String): Path = {
    // NOTE: Probably follows https://www.ietf.org/rfc/rfc3986.txt
    try {
      URI(value);
    } catch {
      case _: URISyntaxException => throw InvalidPath(detail = s"Invalid character contains: ${value}")
      case _ => throw UnexpectedException()
    }

    if (!value.startsWith("/")) {
      s"/${value}"
    } else {
      value
    }
  }
}
