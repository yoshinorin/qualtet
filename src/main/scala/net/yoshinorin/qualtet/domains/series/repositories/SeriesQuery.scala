package net.yoshinorin.qualtet.domains.series

import doobie.Read
import doobie.syntax.all.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update

object SeriesQuery {

  def upsert: doobie.Write[SeriesWriteModel] ?=> doobie.Update[SeriesWriteModel] = {
    val q = s"""
          INSERT INTO series (id, name, title, description)
            VALUES (?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE
            title = VALUES(title),
            description = VALUES(description)
        """
    Update[SeriesWriteModel](q)
  }

  def findByName(name: SeriesName): Read[SeriesReadModel] ?=> Query0[SeriesReadModel] = {
    sql"SELECT * FROM series WHERE name = ${name.value}"
      .query[SeriesReadModel]
  }

  def getAll: Read[SeriesReadModel] ?=> Query0[SeriesReadModel] = {
    sql"SELECT * FROM series"
      .query[SeriesReadModel]
  }

}
