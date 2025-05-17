package net.yoshinorin.qualtet.domains.contentTypes

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.InvalidContentTypeName
import net.yoshinorin.qualtet.syntax.*

import scala.util.matching.Regex

opaque type ContentTypeId = String
object ContentTypeId extends ValueExtender[ContentTypeId] with UlidConvertible[ContentTypeId] {
  given codecContentTypeId: JsonValueCodec[ContentTypeId] = JsonCodecMaker.make
}

opaque type ContentTypeName = String
object ContentTypeName extends ValueExtender[ContentTypeName] {
  given codecContentTypeName: JsonValueCodec[ContentTypeName] = JsonCodecMaker.make
  val contentTypeNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): ContentTypeName = {
    if (!contentTypeNamePattern.matches(value)) {
      throw InvalidContentTypeName(detail = "contentTypeName must be number, alphabet and underscore.")
    }
    value.toLower
  }
}

final case class ContentType(
  id: ContentTypeId = ContentTypeId.apply(),
  name: ContentTypeName
)

object ContentType {
  given codecContentType: JsonValueCodec[ContentType] = JsonCodecMaker.make
  given codecContentTypes: JsonValueCodec[Seq[ContentType]] = JsonCodecMaker.make
}
