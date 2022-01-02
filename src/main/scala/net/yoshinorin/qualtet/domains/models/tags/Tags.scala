package net.yoshinorin.qualtet.domains.models.tags

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import wvlet.airframe.ulid.ULID

final case class TagId(value: String = ULID.newULIDString.toLowerCase) extends AnyVal
object TagId {
  implicit val encodeTagId: Encoder[TagId] = Encoder[String].contramap(_.value)
  implicit val decodeTagId: Decoder[TagId] = Decoder[String].map(TagId.apply)

  def apply(value: String): TagId = {
    ULID.fromString(value)
    new TagId(value)
  }
}

final case class TagName(value: String) extends AnyVal
object TagName {
  implicit val encodeTagName: Encoder[TagName] = deriveEncoder[TagName]
  implicit val decodeTagName: Decoder[TagName] = deriveDecoder[TagName]
  implicit val decodeTagNames: Decoder[List[TagName]] = Decoder.decodeList[TagName]
  implicit val encodeTagNames: Encoder[List[TagName]] = Encoder.encodeList[TagName]
}

final case class Tag(
  id: TagId = new TagId,
  name: TagName
)
