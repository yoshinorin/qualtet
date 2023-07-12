package net.yoshinorin.qualtet.domains.contentTypes

import wvlet.airframe.ulid.ULID
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.syntax.*

opaque type ContentTypeId = String
object ContentTypeId {
  given codecContentTypeId: JsonValueCodec[ContentTypeId] = JsonCodecMaker.make

  def apply(value: String = ULID.newULIDString.toLower): ContentTypeId = {
    val _ = ULID.fromString(value)
    value.toLower
  }

  extension (contentTypeId: ContentTypeId) {
    def value: String = contentTypeId
  }
}

final case class ContentType(
  id: ContentTypeId = ContentTypeId.apply(),
  name: String
)

object ContentType {
  given codecContentType: JsonValueCodec[ContentType] = JsonCodecMaker.make
  given codecContentTypes: JsonValueCodec[Seq[ContentType]] = JsonCodecMaker.make
}
