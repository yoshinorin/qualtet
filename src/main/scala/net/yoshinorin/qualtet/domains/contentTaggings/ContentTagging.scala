package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

final case class ContentTagging(
  contentId: ContentId,
  tagId: TagId
)
