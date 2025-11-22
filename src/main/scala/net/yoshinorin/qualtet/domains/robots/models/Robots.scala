package net.yoshinorin.qualtet.domains.robots

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.ValueExtender
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.errors.InvalidAttributes

opaque type Attributes = String
object Attributes extends ValueExtender[Attributes] {
  given codecAttributes: JsonValueCodec[Attributes] = new JsonValueCodec[Attributes] {
    def decodeValue(in: JsonReader, default: Attributes): Attributes = {
      val str = in.readString(null)
      Attributes(str) match {
        case Right(attrs) => attrs
        case Left(error) => in.decodeError(error.detail)
      }
    }

    def encodeValue(x: Attributes, out: JsonWriter): Unit = {
      out.writeVal(x.value)
    }

    def nullValue: Attributes = null.asInstanceOf[Attributes]
  }

  // https://developers.google.com/search/docs/advanced/robots/robots_meta_tag
  // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta
  val allowedAttributes: List[String] = List("all", "noindex", "nofollow", "none", "noarchive", "nosnippet", "notranslate", "noimageindex")

  def apply(value: String): Either[InvalidAttributes, Attributes] = {
    if (value.endsWith(",")) {
      return Left(InvalidAttributes(detail = "robots.attributes is invalid."))
    }
    val attributes = value.split(",").map(x => x.trim)
    if (attributes.diff(allowedAttributes).length > 0) {
      Left(InvalidAttributes(detail = "robots.attributes is invalid."))
    } else {
      Right(attributes.sorted.mkString(", "))
    }
  }

  /**
   * Create Attributes from a trusted source (e.g., database) without validation.
   *
   * This method should ONLY be used in Repository layer when reading data from the database.
   * Database data is assumed to be already validated at write time, so we skip validation
   * for performance reasons.
   *
   * DO NOT use this method in:
   * - HTTP request handlers
   * - User input processing
   * - Any external data source
   *
   * TODO: restrict scope
   *
   * @param value The raw string value from a trusted source
   * @return The Attributes without validation
   */
  private[domains] def unsafe(value: String): Attributes = value

  extension (attributes: Attributes) {
    def sort: Attributes = attributes.value.split(",").map(x => x.trim).sorted.mkString(", ")
  }
}

final case class Robots(
  contentId: ContentId,
  attributes: Attributes
)

object Robots {
  given codecRobots: JsonValueCodec[Robots] = JsonCodecMaker.make
}
