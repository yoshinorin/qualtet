package net.yoshinorin.qualtet.domains.tags

import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update

object TagQuery {

  def getAll: Query0[ResponseTag] = {
    sql"SELECT * FROM tags"
      .query[ResponseTag]
  }

  def findByName(data: TagName): Query0[Tag] = {
    sql"SELECT * FROM tags WHERE name = $data"
      .query[Tag]
  }

  def findById(id: TagId): Query0[Tag] = {
    sql"SELECT * FROM tags WHERE id = ${id.value}"
      .query[Tag]
  }

  def bulkUpsert: Update[Tag] = {
    val q = s"""
          INSERT INTO tags (id, name)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            name = VALUES(name)
        """
    Update[Tag](q)
  }

  def delete(id: TagId): Query0[Unit] = {
    sql"DELETE FROM tags WHERE id = ${id.value}"
      .query[Unit]
  }

}
