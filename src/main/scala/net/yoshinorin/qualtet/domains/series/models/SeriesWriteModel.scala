package net.yoshinorin.qualtet.domains.series

final case class SeriesWriteModel(
  id: SeriesId,
  name: SeriesName,
  title: String,
  description: Option[String]
)
