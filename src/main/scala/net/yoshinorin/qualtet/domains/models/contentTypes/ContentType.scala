package net.yoshinorin.qualtet.domains.models.contentTypes

import wvlet.airframe.ulid.ULID
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveEncoder

final case class ContentTypeId(value: String = ULID.newULIDString.toLowerCase) extends AnyVal
object ContentTypeId {
  implicit val encodeContentTypeId: Encoder[ContentTypeId] = deriveEncoder[ContentTypeId]
  implicit val decodeContentTypeId: Decoder[ContentTypeId] = Decoder[String].map(ContentTypeId.apply)

  def apply(value: String): ContentTypeId = {
    ULID.fromString(value)
    new ContentTypeId(value)
  }
}

final case class ContentType(
  id: ContentTypeId = new ContentTypeId,
  name: String
)

object ContentType {
  implicit val encodeContentType: Encoder[ContentType] = deriveEncoder[ContentType]
  implicit val encodeContentTypes: Encoder[List[ContentType]] = Encoder.encodeList[ContentType]
}
