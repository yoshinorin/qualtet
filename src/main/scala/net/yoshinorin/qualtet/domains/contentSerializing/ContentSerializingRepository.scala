package net.yoshinorin.qualtet.domains.contentSerializing

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId

trait ContentSerializingRepository[M[_]] {
  def bulkUpsert(data: List[ContentSerializing]): M[Int]
  def findBySeriesId(id: SeriesId): M[Seq[ContentSerializing]]
  def deleteBySeriesId(id: SeriesId): M[Unit]
  def deleteByContentId(id: ContentId): M[Unit]
  def delete(seriesId: SeriesId, contentIds: Seq[ContentId]): M[Unit]
  // TODO: generics
  def fakeRequestInt: M[Int]
  def fakeRequestUnit: M[Unit]
}
