package net.yoshinorin.qualtet.domains.contentTypes

import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update

object DoobieContentTypeQuery {

  def upsert: Update[ContentType] = {
    val q = s"""
          INSERT INTO content_types (id, name)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            name = VALUES(name)
        """
    Update[ContentType](q)
  }

  def getAll: Query0[ContentType] = {
    sql"SELECT * FROM content_types"
      .query[ContentType]
  }

  def findByName(name: String): Query0[ContentType] = {
    sql"SELECT * FROM content_types WHERE name = $name"
      .query[ContentType]
  }
}
