package net.yoshinorin.qualtet.domains.series

import doobie.Read
import doobie.syntax.all.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.{Update, Update0}
import net.yoshinorin.qualtet.domains.contents.ContentId

object SeriesQuery {

  def upsert: doobie.Write[SeriesWriteModel] ?=> doobie.Update[SeriesWriteModel] = {
    val q = s"""
          INSERT INTO series (id, name, path, title, description)
            VALUES (?, ?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE
            title = VALUES(title),
            description = VALUES(description)
        """
    Update[SeriesWriteModel](q)
  }

  def findById(id: SeriesId): Read[SeriesReadModel] ?=> Query0[SeriesReadModel] = {
    sql"SELECT * FROM series WHERE id = ${id.value}"
      .query[SeriesReadModel]
  }

  def findByName(name: SeriesName): Read[SeriesReadModel] ?=> Query0[SeriesReadModel] = {
    sql"SELECT * FROM series WHERE name = ${name.value}"
      .query[SeriesReadModel]
  }

  def findByPath(path: SeriesPath): Read[SeriesReadModel] ?=> Query0[SeriesReadModel] = {
    sql"SELECT * FROM series WHERE path = ${path.value}"
      .query[SeriesReadModel]
  }

  def findByContentId(id: ContentId): Read[SeriesReadModel] ?=> Query0[SeriesReadModel] = {
    sql"""
      SELECT
      DISTINCT
        series.id,
        series.name,
        series.path,
        series.title,
        series.description
      FROM
        series
      INNER JOIN contents_serializing ON
        contents_serializing.series_id = series.id
      INNER JOIN contents ON
        contents.id = contents_serializing.content_id
      WHERE
        contents.id = ${id.value}
    """
      .query[SeriesReadModel]
  }

  def deleteByContentId(id: ContentId): Update0 = {
    sql"""
      DELETE
        series
      FROM
        series
      INNER JOIN contents_serializing ON
        contents_serializing.series_id = series.id
      INNER JOIN contents ON
        contents.id = contents_serializing.content_id
      WHERE
        contents.id = ${id.value}
    """.update
  }

  def deleteBySeriesId(id: SeriesId): Update0 = {
    sql"""
      DELETE
        series
      FROM
        series
      WHERE
        id = ${id.value}
    """.update
  }

  def getAll: Read[SeriesReadModel] ?=> Query0[SeriesReadModel] = {
    sql"SELECT * FROM series"
      .query[SeriesReadModel]
  }

}
