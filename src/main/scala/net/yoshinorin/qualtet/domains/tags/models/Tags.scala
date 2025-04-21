package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.{InvalidPath, UnexpectedException}
import java.net.{URI, URISyntaxException}

opaque type TagId = String
object TagId extends ValueExtender[TagId] with UlidConvertible[TagId] {
  given codecTagId: JsonValueCodec[TagId] = JsonCodecMaker.make
}

opaque type TagName = String
object TagName extends ValueExtender[TagName] {
  given codecTagName: JsonValueCodec[TagName] = JsonCodecMaker.make
  given codecTagNames: JsonValueCodec[Seq[TagName]] = JsonCodecMaker.make

  def apply(value: String): TagName = value
}

opaque type TagPath = String
object TagPath extends ValueExtender[TagPath] {
  given codecTagPath: JsonValueCodec[TagPath] = JsonCodecMaker.make

  def apply(value: String): TagPath = {
    // NOTE: Probably follows https://www.ietf.org/rfc/rfc3986.txt
    try {
      URI(value);
    } catch {
      case _: URISyntaxException => throw InvalidPath(detail = s"Invalid character contains: ${value}")
      case _ => throw UnexpectedException()
    }
    value
  }
}

final case class Tag(
  id: TagId = TagId.apply(),
  name: TagName,
  path: TagPath
)
object Tag {
  given codecTag: JsonValueCodec[Tag] = JsonCodecMaker.make
  given codecTags: JsonValueCodec[Option[Seq[Tag]]] = JsonCodecMaker.make
}
