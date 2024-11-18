package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.contents.{ContentId, Path}

final case class ArticleReadModel(
  id: ContentId,
  path: Path,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)
