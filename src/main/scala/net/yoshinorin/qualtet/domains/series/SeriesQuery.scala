package net.yoshinorin.qualtet.domains.series

import doobie.Read
import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import net.yoshinorin.qualtet.domains.contents.Path

object SeriesQuery {

  def findByPath(path: Path)(implicit tagRead: Read[Series]): Query0[Series] = {
    sql"SELECT * FROM series WHERE path = 'test'"
      .query[Series]
  }

}

