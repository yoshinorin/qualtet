package net.yoshinorin.qualtet.domains.search

import net.yoshinorin.qualtet.domains.contents.Path

final case class SearchResuletReadModel(
  path: Path,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)
