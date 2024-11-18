package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contents.Path

final case class ArchiveReadModel(
  path: Path,
  title: String,
  publishedAt: Long
)
