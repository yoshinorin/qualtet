package net.yoshinorin.qualtet.domains.contentSerializing

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId

final case class ContentSerializingWriteModel(
  seriesId: SeriesId,
  contentId: ContentId
)
