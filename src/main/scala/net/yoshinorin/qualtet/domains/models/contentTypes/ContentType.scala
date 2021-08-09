package net.yoshinorin.qualtet.domains.models.contentTypes

import java.util.UUID
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class ContentType(
  id: String = UUID.randomUUID().toString,
  name: String
)

object ContentType {
  implicit val encodeContentType: Encoder[ContentType] = deriveEncoder[ContentType]
  implicit val encodeContentTypes: Encoder[List[ContentType]] = Encoder.encodeList[ContentType]
}
