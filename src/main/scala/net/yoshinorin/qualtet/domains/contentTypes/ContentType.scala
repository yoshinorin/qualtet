package net.yoshinorin.qualtet.domains.contentTypes

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{ValueExtender, UlidConvertible}
import net.yoshinorin.qualtet.syntax.*

opaque type ContentTypeId = String
object ContentTypeId extends ValueExtender[ContentTypeId] with UlidConvertible[ContentTypeId] {
  given codecContentTypeId: JsonValueCodec[ContentTypeId] = JsonCodecMaker.make
}

final case class ContentType(
  id: ContentTypeId = ContentTypeId.apply(),
  name: String
)

object ContentType {
  given codecContentType: JsonValueCodec[ContentType] = JsonCodecMaker.make
  given codecContentTypes: JsonValueCodec[Seq[ContentType]] = JsonCodecMaker.make
}
