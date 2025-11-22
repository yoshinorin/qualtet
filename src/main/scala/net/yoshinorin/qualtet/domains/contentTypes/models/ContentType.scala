package net.yoshinorin.qualtet.domains.contentTypes

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.InvalidContentTypeName
import net.yoshinorin.qualtet.syntax.*

import scala.util.matching.Regex

opaque type ContentTypeId = String
object ContentTypeId extends ValueExtender[ContentTypeId] with UlidConvertible[ContentTypeId] {
  given codecContentTypeId: JsonValueCodec[ContentTypeId] = JsonCodecMaker.make
}

opaque type ContentTypeName = String
object ContentTypeName extends ValueExtender[ContentTypeName] {
  given codecContentTypeName: JsonValueCodec[ContentTypeName] = JsonCodecMaker.make
  val contentTypeNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): Either[InvalidContentTypeName, ContentTypeName] = {
    if (!contentTypeNamePattern.matches(value)) {
      Left(InvalidContentTypeName(detail = "contentTypeName must be number, alphabet and underscore."))
    } else {
      Right(value.toLower)
    }
  }

  /**
   * Create a ContentTypeName from a trusted source (e.g., database) without validation.
   *
   * This method should ONLY be used in Repository layer when reading data from the database.
   * Database data is assumed to be already validated at write time, so we skip validation
   * for performance reasons while still applying normalization (toLowerCase) for consistency.
   *
   * DO NOT use this method in:
   * - HTTP request handlers
   * - User input processing
   * - Any external data source
   *
   * @param value The raw string value from a trusted source
   * @return The normalized ContentTypeName without validation
   */
  private[contentTypes] def unsafe(value: String): ContentTypeName = value.toLower
}

final case class ContentType(
  id: ContentTypeId = ContentTypeId.apply(),
  name: ContentTypeName
)

object ContentType {
  given codecContentType: JsonValueCodec[ContentType] = JsonCodecMaker.make
  given codecContentTypes: JsonValueCodec[Seq[ContentType]] = JsonCodecMaker.make
}
