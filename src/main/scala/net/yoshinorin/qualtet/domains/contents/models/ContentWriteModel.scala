package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.robots.Attributes

final case class ContentWriteModel(
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
