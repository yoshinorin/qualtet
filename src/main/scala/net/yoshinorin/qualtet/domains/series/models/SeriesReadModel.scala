package net.yoshinorin.qualtet.domains.series

final case class SeriesReadModel(
  id: SeriesId,
  path: SeriesPath,
  title: String,
  description: Option[String]
)
