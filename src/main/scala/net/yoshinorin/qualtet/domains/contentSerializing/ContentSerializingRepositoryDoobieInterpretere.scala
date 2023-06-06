package net.yoshinorin.qualtet.domains.contentSerializing

import doobie.{Read, Write}
import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId

class ContentSerializingRepositoryDoobieInterpretere extends ContentSerializingRepository[ConnectionIO] with ConnectionIOFaker {

  given contentSerializingRead: Read[ContentSerializing] =
    Read[(String, String)].map { case (seriesId, contentId) => ContentSerializing(SeriesId(seriesId), ContentId(contentId)) }

  given contentSerializingWithOptionRead: Read[Option[ContentSerializing]] =
    Read[(String, String)].map { case (seriesId, contentId) => Some(ContentSerializing(SeriesId(seriesId), ContentId(contentId))) }

  given contentSerializingWrite: Write[ContentSerializing] =
    Write[(String, String)].contramap(s => (s.seriesId.value, s.contentId.value))

  override def bulkUpsert(data: List[ContentSerializing]): ConnectionIO[Int] = {
    ContentSerializingQuery.bulkUpsert.updateMany(data)
  }
  override def findBySeriesId(id: SeriesId): ConnectionIO[Seq[ContentSerializing]] = {
    ContentSerializingQuery.findBySeriesId(id).to[Seq]
  }

  override def deleteBySeriesId(id: SeriesId): ConnectionIO[Unit] = {
    ContentSerializingQuery.deleteBySeriesId(id).option.map(_ => ())
  }

  override def deleteByContentId(id: ContentId): ConnectionIO[Unit] = {
    ContentSerializingQuery.deleteByContentId(id).option.map(_ => ())
  }

  override def delete(seriesId: SeriesId, contentIds: Seq[ContentId]): ConnectionIO[Unit] = {
    ContentSerializingQuery.delete(seriesId, contentIds).option.map(_ => ())
  }
  override def fakeRequestInt: ConnectionIO[Int] = ConnectionIOWithInt
  override def fakeRequestUnit: ConnectionIO[Unit] = ConnectionIOWithUnit
}
