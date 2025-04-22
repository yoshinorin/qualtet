package net.yoshinorin.qualtet.domains.search

import net.yoshinorin.qualtet.domains.contents.ContentPath

final case class SearchResuletReadModel(
  path: ContentPath,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)
