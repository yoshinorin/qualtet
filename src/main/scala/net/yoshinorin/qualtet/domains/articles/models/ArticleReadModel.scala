package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.contents.ContentId

final case class ArticleReadModel(
  id: ContentId,
  path: ContentPath,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)
