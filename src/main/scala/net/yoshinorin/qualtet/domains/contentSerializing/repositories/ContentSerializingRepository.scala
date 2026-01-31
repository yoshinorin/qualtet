package net.yoshinorin.qualtet.domains.contentSerializing

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId

trait ContentSerializingRepository[F[_]] {
  def bulkUpsert(data: List[ContentSerializingWriteModel]): F[Int]
  def findBySeriesId(id: SeriesId): F[Seq[ContentSerializingReadModel]]
  def findByContentId(id: ContentId): F[Option[ContentSerializingReadModel]]
  def deleteBySeriesId(id: SeriesId): F[Unit]
  def deleteByContentId(id: ContentId): F[Unit]
  def delete(seriesId: SeriesId, contentIds: Seq[ContentId]): F[Unit]
}

object ContentSerializingRepository {

  import doobie.{Read, Write}
  import doobie.ConnectionIO

  given ContentSerializingRepository: ContentSerializingRepository[ConnectionIO] = {
    new ContentSerializingRepository[ConnectionIO] {
      given contentSerializingRead: Read[ContentSerializingReadModel] =
        Read[(String, String)].map { case (seriesId, contentId) => ContentSerializingReadModel(SeriesId(seriesId), ContentId(contentId)) }

      given contentSerializingWrite: Write[ContentSerializingWriteModel] =
        Write[(String, String)].contramap(s => (s.seriesId.value, s.contentId.value))

      override def bulkUpsert(data: List[ContentSerializingWriteModel]): ConnectionIO[Int] = {
        ContentSerializingQuery.bulkUpsert.updateMany(data)
      }

      override def findBySeriesId(id: SeriesId): ConnectionIO[Seq[ContentSerializingReadModel]] = {
        ContentSerializingQuery.findBySeriesId(id).to[Seq]
      }

      override def findByContentId(id: ContentId): ConnectionIO[Option[ContentSerializingReadModel]] = {
        ContentSerializingQuery.findByContentId(id).option
      }

      override def deleteBySeriesId(id: SeriesId): ConnectionIO[Unit] = {
        ContentSerializingQuery.deleteBySeriesId(id).run.map(_ => ())
      }

      override def deleteByContentId(id: ContentId): ConnectionIO[Unit] = {
        ContentSerializingQuery.deleteByContentId(id).run.map(_ => ())
      }

      override def delete(seriesId: SeriesId, contentIds: Seq[ContentId]): ConnectionIO[Unit] = {
        ContentSerializingQuery.delete(seriesId, contentIds).run.map(_ => ())
      }
    }
  }

}
