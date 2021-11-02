package net.yoshinorin.qualtet.domains.models.externalResources

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.yoshinorin.qualtet.domains.models.contents.ContentId

final case class ExternalResourceKind(value: String) extends AnyVal
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

final case class ExternalResource(
  contentId: ContentId,
  kind: ExternalResourceKind,
  name: String
)
object ExternalResource {
  implicit val encodeExternalResource: Encoder[ExternalResource] = deriveEncoder[ExternalResource]
  implicit val decodeExternalResource: Decoder[ExternalResource] = deriveDecoder[ExternalResource]
}

final case class ExternalResources(
  kind: ExternalResourceKind,
  values: List[String]
)
object ExternalResources {
  implicit val encodeExternalResources: Encoder[ExternalResources] = deriveEncoder[ExternalResources]
  implicit val decodeExternalResources: Decoder[ExternalResources] = deriveDecoder[ExternalResources]
}
