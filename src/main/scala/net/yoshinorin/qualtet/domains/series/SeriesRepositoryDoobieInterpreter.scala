package net.yoshinorin.qualtet.domains.series

import doobie.ConnectionIO
import doobie.Read
import net.yoshinorin.qualtet.domains.contents.Path

class SeriesRepositoryDoobieInterpreter extends SeriesRepository[ConnectionIO] {

  given seriesRead: Read[Series] =
    Read[(String, String, String, Option[String])].map {
      case (seriesId, path, title, description) =>
        Series(
          SeriesId(seriesId),
          Path(path),
          title,
          description
        )
    }

  given seriesWithOptionRead: Read[Option[Series]] =
    Read[(String, String, String, Option[String])].map {
      case (seriesId, path, title, description) =>
        Some(
        Series(
          SeriesId(seriesId),
          Path(path),
          title,
          description
        ))
    }

  override def findByPath(path: Path): ConnectionIO[Option[Series]] = {
    SeriesQuery.findByPath(path).option
  }
}

