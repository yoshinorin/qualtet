package net.yoshinorin.qualtet.domains.models.contentTypes

import wvlet.airframe.ulid.ULID
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveEncoder
import net.yoshinorin.qualtet.domains.models.ResponseBase

final case class ContentTypeId(value: String = ULID.newULIDString) extends AnyVal
object ContentTypeId {
  implicit val encodeContentTypeId: Encoder[ContentTypeId] = deriveEncoder[ContentTypeId]
  implicit val decodeContentTypeId: Decoder[ContentTypeId] = Decoder[String].map(ContentTypeId.apply)

  def apply(value: String): ContentTypeId = {
    // TODO: declare exception
    ULID.fromString(value)
    new ContentTypeId(value)
  }
}

final case class ContentType(
  id: ContentTypeId = new ContentTypeId,
  name: String
) extends ResponseBase

object ContentType {
  implicit val encodeContentType: Encoder[ContentType] = deriveEncoder[ContentType]
  implicit val encodeContentTypes: Encoder[List[ContentType]] = Encoder.encodeList[ContentType]
}
