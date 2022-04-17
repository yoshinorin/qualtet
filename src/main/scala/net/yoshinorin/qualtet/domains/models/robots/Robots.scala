package net.yoshinorin.qualtet.domains.models.robots

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import net.yoshinorin.qualtet.domains.models.Fail.UnprocessableEntity
import net.yoshinorin.qualtet.domains.models.contents.ContentId

final case class Attributes(value: String) extends AnyVal
object Attributes {

  implicit val encodeAttributes: Encoder[Attributes] = Encoder[String].contramap(_.value)
  implicit val decodeAttributes: Decoder[Attributes] = Decoder[String].map(Attributes.apply)

  // https://developers.google.com/search/docs/advanced/robots/robots_meta_tag
  // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta
  val allowedAttributes: List[String] = List("all", "noindex", "nofollow", "none", "noarchive", "nosnippet", "notranslate", "noimageindex")

  def apply(value: String): Attributes = {
    if (value.endsWith(",")) {
      throw UnprocessableEntity("robots.attributes is invalid.")
    }

    val attributes = value.split(',').map(x => x.trim)
    if (attributes.diff(allowedAttributes).length > 0) {
      throw UnprocessableEntity("robots.attributes is invalid.")
    } else {
      new Attributes(attributes.sorted.mkString(", "))
    }
  }
}

final case class Robots(
  contentId: ContentId,
  attributes: Attributes
)

object Robots {
  implicit val encodeRobots: Encoder[Robots] = deriveEncoder[Robots]
  implicit val decodeRobots: Decoder[Robots] = deriveDecoder[Robots]
}
