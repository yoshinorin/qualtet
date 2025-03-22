package net.yoshinorin.qualtet.domains.contentSerializing

import cats.data.NonEmptyList
import doobie.{Read, Write}
import doobie.syntax.all.toSqlInterpolator
import doobie.util.update.{Update, Update0}
import doobie.util.{fragments, query}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId

object ContentSerializingQuery {

  def findBySeriesId(id: SeriesId): Read[ContentSerializingReadModel] ?=> query.Query0[ContentSerializingReadModel] = {
    sql"SELECT * FROM contents_serializing WHERE series_id = ${id.value}"
      .query[ContentSerializingReadModel]
  }

  def findByContentId(id: ContentId): Read[ContentSerializingReadModel] ?=> query.Query0[ContentSerializingReadModel] = {
    sql"SELECT * FROM contents_serializing WHERE content_id = ${id.value}"
      .query[ContentSerializingReadModel]
  }

  def bulkUpsert: Write[ContentSerializingWriteModel] ?=> Update[ContentSerializingWriteModel] = {
    val q = s"""
          INSERT INTO contents_serializing (series_id, content_id)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            series_id = VALUES(series_id),
            content_id = VALUES(content_id)
        """
    Update[ContentSerializingWriteModel](q)
  }

  def deleteBySeriesId(id: SeriesId): Update0 = {
    sql"DELETE FROM contents_serializing WHERE series_id = ${id.value}".update
  }

  def deleteByContentId(id: ContentId): Update0 = {
    sql"DELETE FROM contents_serializing WHERE content_id = ${id.value}".update
  }

  def delete(seriesId: SeriesId, contentIds: Seq[ContentId]): Update0 = {
    val query = fr"""
      DELETE FROM contents_serializing
      WHERE series_id = ${seriesId.value}
      AND """ ++ fragments.in(fr"content_id", NonEmptyList.fromList(contentIds.toList.map(c => c.value)).get)

    query.update
  }

}
