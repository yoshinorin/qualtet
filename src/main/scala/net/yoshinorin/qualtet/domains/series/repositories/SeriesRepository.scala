package net.yoshinorin.qualtet.domains.series

import net.yoshinorin.qualtet.domains.contents.ContentId

trait SeriesRepository[F[_]] {
  def upsert(data: SeriesWriteModel): F[Int]
  def findByName(name: SeriesName): F[Option[SeriesReadModel]]
  def findByContentId(id: ContentId): F[Option[SeriesReadModel]]
  def deleteByContentId(id: ContentId): F[Unit]
  def getAll(): F[Seq[SeriesReadModel]]
}

object SeriesRepository {

  import doobie.ConnectionIO
  import doobie.{Read, Write}

  given SeriesRepository: SeriesRepository[ConnectionIO] = {
    new SeriesRepository[ConnectionIO] {

      given seriesWrite: Write[SeriesWriteModel] =
        Write[(String, String, String, Option[String])].contramap { s =>
          (s.id.value, s.name.value, s.title, s.description)
        }

      given seriesRead: Read[SeriesReadModel] =
        Read[(String, String, String, Option[String])].map { case (seriesId, name, title, description) =>
          SeriesReadModel(
            SeriesId(seriesId),
            SeriesName(name),
            title,
            description
          )
        }

      given seriesOrOptionRead: Read[Option[SeriesReadModel]] =
        Read[(String, String, String, Option[String])].map { case (seriesId, name, title, description) =>
          Some(
            SeriesReadModel(
              SeriesId(seriesId),
              SeriesName(name),
              title,
              description
            )
          )
        }

      // TODO: do not `run` here
      override def upsert(data: SeriesWriteModel): ConnectionIO[Int] = {
        SeriesQuery.upsert.run(data)
      }

      override def findByName(name: SeriesName): ConnectionIO[Option[SeriesReadModel]] = {
        SeriesQuery.findByName(name).option
      }

      override def findByContentId(id: ContentId): ConnectionIO[Option[SeriesReadModel]] = {
        SeriesQuery.findByContentId(id).option
      }

      override def deleteByContentId(id: ContentId): ConnectionIO[Unit] = {
        SeriesQuery.deleteByContentId(id).run.map(_ => ())
      }

      override def getAll(): ConnectionIO[Seq[SeriesReadModel]] = {
        SeriesQuery.getAll.to[Seq]
      }

    }
  }

}
