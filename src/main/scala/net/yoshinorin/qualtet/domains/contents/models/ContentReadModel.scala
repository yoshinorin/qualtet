package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.robots.Attributes

final case class ContentReadModel(
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
