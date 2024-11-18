package net.yoshinorin.qualtet.domains.contentSerializing

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId

final case class ContentSerializingReadModel(
  seriesId: SeriesId,
  contentId: ContentId
)
