package net.yoshinorin.qualtet.domains.contents

import java.time.ZonedDateTime
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{Path, UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.robots.Attributes

opaque type ContentId = String
object ContentId extends ValueExtender[ContentId] with UlidConvertible[ContentId] {
  given codecContentId: JsonValueCodec[ContentId] = JsonCodecMaker.make
}

final case class Content(
  id: ContentId = ContentId.apply(),
  authorId: AuthorId,
  contentTypeId: ContentTypeId,
  path: Path,
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
