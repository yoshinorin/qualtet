package net.yoshinorin.qualtet.domains.contentSerializing

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId

trait ContentSerializingRepository[F[_]] {
  def upsert(data: ContentSerializing): F[Int]
  def bulkUpsert(data: List[ContentSerializing]): F[Int]
  def findBySeriesId(id: SeriesId): F[Seq[ContentSerializing]]
  def deleteBySeriesId(id: SeriesId): F[Unit]
  def deleteByContentId(id: ContentId): F[Unit]
  def delete(seriesId: SeriesId, contentIds: Seq[ContentId]): F[Unit]
  // TODO: generics
  def fakeRequestInt: F[Int]
  def fakeRequestUnit: F[Unit]
}

object ContentSerializingRepository {

  import cats.implicits.catsSyntaxApplicativeId
  import doobie.{Read, Write}
  import doobie.ConnectionIO

  given ContentSerializingRepository: ContentSerializingRepository[ConnectionIO] = {
    new ContentSerializingRepository[ConnectionIO] {
      given contentSerializingRead: Read[ContentSerializing] =
        Read[(String, String)].map { case (seriesId, contentId) => ContentSerializing(SeriesId(seriesId), ContentId(contentId)) }

      given contentSerializingWithOptionRead: Read[Option[ContentSerializing]] =
        Read[(String, String)].map { case (seriesId, contentId) => Some(ContentSerializing(SeriesId(seriesId), ContentId(contentId))) }

      given contentSerializingWrite: Write[ContentSerializing] =
        Write[(String, String)].contramap(s => (s.seriesId.value, s.contentId.value))

      override def upsert(data: ContentSerializing): ConnectionIO[Int] = {
        ContentSerializingQuery.bulkUpsert.run(data)
      }

      override def bulkUpsert(data: List[ContentSerializing]): ConnectionIO[Int] = {
        ContentSerializingQuery.bulkUpsert.updateMany(data)
      }

      override def findBySeriesId(id: SeriesId): ConnectionIO[Seq[ContentSerializing]] = {
        ContentSerializingQuery.findBySeriesId(id).to[Seq]
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
      override def fakeRequestInt: ConnectionIO[Int] = 0.pure[ConnectionIO]
      override def fakeRequestUnit: ConnectionIO[Unit] = ().pure[ConnectionIO]
    }
  }

}
