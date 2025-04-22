package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contents.ContentPath

final case class ArchiveReadModel(
  path: ContentPath,
  title: String,
  publishedAt: Long
)
