package net.yoshinorin.qualtet.domains.series

final case class SeriesReadModel(
  id: SeriesId,
  name: SeriesName,
  path: SeriesPath,
  title: String,
  description: Option[String]
)
