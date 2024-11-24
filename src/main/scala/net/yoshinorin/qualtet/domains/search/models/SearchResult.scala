package net.yoshinorin.qualtet.domains.search

import net.yoshinorin.qualtet.domains.Path

final case class SearchResult(
  path: Path,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)
