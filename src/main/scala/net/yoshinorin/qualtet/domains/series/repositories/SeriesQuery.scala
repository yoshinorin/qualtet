package net.yoshinorin.qualtet.domains.series

import doobie.{Read, Write}
import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update

object SeriesQuery {

  def upsert: doobie.Write[Series] ?=> doobie.Update[Series] = {
    val q = s"""
          INSERT INTO series (id, name, title, description)
            VALUES (?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE
            title = VALUES(title),
            description = VALUES(description)
        """
    Update[Series](q)
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
