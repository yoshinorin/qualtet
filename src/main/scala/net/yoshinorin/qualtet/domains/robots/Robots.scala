package net.yoshinorin.qualtet.domains.robots

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.message.Fail.UnprocessableEntity

opaque type Attributes = String
object Attributes {
  given codecAttributes: JsonValueCodec[Attributes] = JsonCodecMaker.make

  // https://developers.google.com/search/docs/advanced/robots/robots_meta_tag
  // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta
  val allowedAttributes: List[String] = List("all", "noindex", "nofollow", "none", "noarchive", "nosnippet", "notranslate", "noimageindex")

  def apply(value: String): Attributes = {
    if (value.endsWith(",")) {
      throw UnprocessableEntity("robots.attributes is invalid.")
    }

    val attributes = value.split(",").map(x => x.trim)
    if (attributes.diff(allowedAttributes).length > 0) {
      throw UnprocessableEntity("robots.attributes is invalid.")
    } else {
      attributes.sorted.mkString(", ")
    }
  }

  extension (attributes: Attributes) {
    def value: String = attributes
    def sort: Attributes = value.split(",").map(x => x.trim).sorted.mkString(", ")
  }
}

final case class Robots(
  contentId: ContentId,
  attributes: Attributes
)

object Robots {
  given codecRobots: JsonValueCodec[Robots] = JsonCodecMaker.make
}
