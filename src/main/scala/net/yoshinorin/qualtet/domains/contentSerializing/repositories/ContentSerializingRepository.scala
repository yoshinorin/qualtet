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
