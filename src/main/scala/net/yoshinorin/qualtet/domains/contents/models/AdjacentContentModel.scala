package net.yoshinorin.qualtet.domains.contents

final case class AdjacentContentModel(
  id: ContentId,
  path: ContentPath,
  title: String,
  publishedAt: Long
)
