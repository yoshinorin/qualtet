package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.InvalidPath

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

  private lazy val unusableChars = "[:?#@!$&'()*+,;=<>\"\\\\^`{}|~]"
  private lazy val invalidPercentRegex = ".*%(?![0-9A-Fa-f]{2}).*"

  def apply(value: String): TagPath = {
    if (unusableChars.r.findFirstMatchIn(value).isDefined) {
      throw InvalidPath(detail = s"Invalid character contains: ${value}")
    }

    if (invalidPercentRegex.r.matches(value)) {
      throw InvalidPath(detail = s"Invalid percent encoding in path: ${value}")
    }

    if (!value.startsWith("/")) {
      s"/${value}"
    } else {
      value
    }
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
