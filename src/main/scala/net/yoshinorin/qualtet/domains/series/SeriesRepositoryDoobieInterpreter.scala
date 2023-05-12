package net.yoshinorin.qualtet.domains.series

import doobie.ConnectionIO
import doobie.{Read, Write}

class SeriesRepositoryDoobieInterpreter extends SeriesRepository[ConnectionIO] {

  given seriesWrite: Write[Series] =
    Write[(String, String, String, Option[String])].contramap { s =>
      (s.id.value, s.name.value, s.title, s.description)
    }

  given seriesRead: Read[Series] =
    Read[(String, String, String, Option[String])].map { case (seriesId, name, title, description) =>
      Series(
        SeriesId(seriesId),
        SeriesName(name),
        title,
        description
      )
    }

  given seriesWithOptionRead: Read[Option[Series]] =
    Read[(String, String, String, Option[String])].map { case (seriesId, name, title, description) =>
      Some(
        Series(
          SeriesId(seriesId),
          SeriesName(name),
          title,
          description
        )
      )
    }

  // TODO: do not `run` here
  override def upsert(data: Series): ConnectionIO[Int] = {
    SeriesQuery.upsert.run(data)
  }

  override def findByName(name: SeriesName): ConnectionIO[Option[Series]] = {
    SeriesQuery.findByName(name).option
  }

  override def getAll(): ConnectionIO[Seq[Series]] = {
    SeriesQuery.getAll.to[Seq]
  }
}
