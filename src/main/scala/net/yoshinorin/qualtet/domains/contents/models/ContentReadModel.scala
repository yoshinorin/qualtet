package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.robots.Attributes

final case class ContentWithMetaReadModel(
  id: ContentId,
  title: String,
  robotsAttributes: Attributes,
  externalResourceKindKeys: Option[String],
  externalResourceKindValues: Option[String],
  tagIds: Option[String],
  tagNames: Option[String],
  content: String,
  authorName: AuthorName,
  publishedAt: Long,
  updatedAt: Long
)

final case class ContentReadModel(
  id: ContentId,
  authorId: AuthorId,
  contentTypeId: ContentTypeId,
  path: Path,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long,
  updatedAt: Long
)
