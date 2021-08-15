package net.yoshinorin.qualtet.domains.models.contentTypes

import java.util.UUID
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveEncoder
import net.yoshinorin.qualtet.domains.models.ResponseBase

final case class ContentTypeId(value: String = UUID.randomUUID().toString) extends AnyVal
object ContentTypeId {
  implicit val encodeContentTypeId: Encoder[ContentTypeId] = deriveEncoder[ContentTypeId]
  implicit val decodeContentTypeId: Decoder[ContentTypeId] = Decoder[String].map(ContentTypeId.apply)

  def apply(value: String): ContentTypeId = {
    // TODO: declare exception
    UUID.fromString(value)
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
