package net.yoshinorin.qualtet.domains.series

final case class SeriesWriteModel(
  id: SeriesId,
  path: SeriesPath,
  title: String,
  description: Option[String]
)
