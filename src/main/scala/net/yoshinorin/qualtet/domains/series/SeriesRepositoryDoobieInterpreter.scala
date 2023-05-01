package net.yoshinorin.qualtet.domains.series

import doobie.ConnectionIO
import doobie.{Read, Write}
import net.yoshinorin.qualtet.domains.contents.Path

class SeriesRepositoryDoobieInterpreter extends SeriesRepository[ConnectionIO] {

  given seriesWrite: Write[Series] =
    Write[(String, String, String, Option[String])].contramap { s =>
      (s.id.value, s.path.value, s.title, s.description)
    }

  given seriesRead: Read[Series] =
    Read[(String, String, String, Option[String])].map { case (seriesId, path, title, description) =>
      Series(
        SeriesId(seriesId),
        Path(path),
        title,
        description
      )
    }

  given seriesWithOptionRead: Read[Option[Series]] =
    Read[(String, String, String, Option[String])].map { case (seriesId, path, title, description) =>
      Some(
        Series(
          SeriesId(seriesId),
          Path(path),
          title,
          description
        )
      )
    }

  // TODO: do not `run` here
  override def upsert(data: Series): ConnectionIO[Int] = {
    SeriesQuery.upsert.run(data)
  }

  override def findByPath(path: Path): ConnectionIO[Option[Series]] = {
    SeriesQuery.findByPath(path).option
  }

  override def getAll(): ConnectionIO[Seq[Series]] = {
    SeriesQuery.getAll.to[Seq]
  }
}
