package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.{PathValidator, Request, UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.{DomainError, InvalidPath}

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

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
object TagPath extends ValueExtender[TagPath] with PathValidator {
  given codecTagPath: JsonValueCodec[TagPath] = JsonCodecMaker.make

  def apply(value: String): Either[InvalidPath, TagPath] = {
    validate(value).map(_ => normalize(value))
  }

  private[domains] def fromTrusted(value: String): TagPath = normalize(value)
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
