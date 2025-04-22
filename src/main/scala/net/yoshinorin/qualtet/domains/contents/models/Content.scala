package net.yoshinorin.qualtet.domains.contents

import java.time.ZonedDateTime
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.errors.{InvalidPath, UnexpectedException}
import java.net.{URI, URISyntaxException}

opaque type ContentId = String
object ContentId extends ValueExtender[ContentId] with UlidConvertible[ContentId] {
  given codecContentId: JsonValueCodec[ContentId] = JsonCodecMaker.make
}

opaque type ContentPath = String
object ContentPath extends ValueExtender[ContentPath] {
  given codecPath: JsonValueCodec[ContentPath] = JsonCodecMaker.make

  def apply(value: String): ContentPath = {
    // NOTE: Probably follows https://www.ietf.org/rfc/rfc3986.txt
    try {
      URI(value);
    } catch {
      case _: URISyntaxException => throw InvalidPath(detail = s"Invalid character contains: ${value}")
      case _ => throw UnexpectedException()
    }

    if (!value.startsWith("/")) {
      s"/${value}"
    } else {
      value
    }
  }
}

final case class Content(
  id: ContentId = ContentId.apply(),
  authorId: AuthorId,
  contentTypeId: ContentTypeId,
  path: ContentPath,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
)

final case class ContentWithMeta(
  id: ContentId,
  title: String,
  robotsAttributes: Attributes,
  externalResourceKindKeys: Option[String],
  externalResourceKindValues: Option[String],
  tagIds: Option[String],
  tagNames: Option[String],
  tagPaths: Option[String],
  content: String,
  authorName: AuthorName,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
)
