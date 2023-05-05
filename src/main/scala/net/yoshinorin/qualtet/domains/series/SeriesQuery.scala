package net.yoshinorin.qualtet.domains.series

import doobie.{Read, Write}
import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update
import net.yoshinorin.qualtet.domains.contents.Path

object SeriesQuery {

  def upsert(implicit seriesWrite: doobie.Write[Series]): doobie.Update[Series] = {
    val q = s"""
          INSERT INTO series (id, path, title, description)
            VALUES (?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE
            title = VALUES(title),
            description = VALUES(description)
        """
    Update[Series](q)
  }

  def findByPath(path: Path)(implicit tagRead: Read[Series]): Query0[Series] = {
    sql"SELECT * FROM series WHERE path = ${path.value}"
      .query[Series]
  }

  def getAll(implicit tagRead: Read[Series]): Query0[Series] = {
    sql"SELECT * FROM series"
      .query[Series]
  }

}