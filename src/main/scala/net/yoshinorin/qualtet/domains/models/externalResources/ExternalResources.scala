package net.yoshinorin.qualtet.domains.models.externalResources

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.yoshinorin.qualtet.domains.models.contents.ContentId

final case class ExternalResourceKind(value: String)
object ExternalResourceKind {
  implicit val encodeExternalResourceKind: Encoder[ExternalResourceKind] = Encoder[String].contramap(_.value)
  implicit val decodeExternalResourceKind: Decoder[ExternalResourceKind] = Decoder[String].map(ExternalResourceKind.apply)

  val allowedKinds = List("js", "css")
  def apply(value: String): ExternalResourceKind = {
    if (!allowedKinds.contains(value)) {
      // TODO: declare exception
      throw new Exception("TODO")
    }
    new ExternalResourceKind(value)
  }
}

final case class ExternalResources(
  contentId: ContentId,
  kind: ExternalResourceKind,
  names: List[String]
)
object ExternalResources {
  implicit val encodeExternalResources: Encoder[ExternalResources] = deriveEncoder[ExternalResources]
  implicit val decodeExternalResources: Decoder[ExternalResources] = deriveDecoder[ExternalResources]
}
