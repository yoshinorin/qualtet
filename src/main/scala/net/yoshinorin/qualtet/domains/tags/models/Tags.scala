package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{FromTrustedSource, Request, UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.{DomainError, InvalidPath}

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

  def apply(value: String): Either[InvalidPath, TagPath] = {
    if (unusableChars.r.findFirstMatchIn(value).isDefined) {
      return Left(InvalidPath(detail = s"Invalid character contains: ${value}"))
    }
    if (invalidPercentRegex.r.matches(value)) {
      return Left(InvalidPath(detail = s"Invalid percent encoding in path: ${value}"))
    }
    val normalized = if (!value.startsWith("/")) s"/${value}" else value
    Right(normalized)
  }

  private def unsafeFrom(value: String): TagPath = {
    if (!value.startsWith("/")) s"/${value}" else value
  }

  given fromTrustedSource: FromTrustedSource[TagPath] with {
    def fromTrusted(value: String): TagPath = unsafeFrom(value)
  }
}

final case class Tag(
  id: TagId = TagId.apply(),
  name: TagName,
  path: TagPath
) extends Request[Tag] {
  def postDecode: Either[DomainError, Tag] = {
    TagPath(path.value).map(tagPath => Tag(id, name, tagPath))
  }
}
object Tag {
  given codecTag: JsonValueCodec[Tag] = JsonCodecMaker.make
  given codecTags: JsonValueCodec[Option[Seq[Tag]]] = JsonCodecMaker.make
}
