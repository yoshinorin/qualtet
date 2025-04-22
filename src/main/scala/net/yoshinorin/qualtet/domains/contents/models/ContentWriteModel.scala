package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.authors.AuthorId
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

final case class ContentWriteModel(
  id: ContentId,
  authorId: AuthorId,
  contentTypeId: ContentTypeId,
  path: ContentPath,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long,
  updatedAt: Long
)
