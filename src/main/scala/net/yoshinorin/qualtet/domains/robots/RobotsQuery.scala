package net.yoshinorin.qualtet.domains.robots

import doobie.implicits.toSqlInterpolator
import doobie.util.update.Update
import doobie.util.query
import net.yoshinorin.qualtet.domains.contents.ContentId

object RobotsQuery {

  def upsert: Update[Robots] = {
    val q = s"""
          INSERT INTO robots (content_id, attributes)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            attributes = VALUES(attributes)
        """
    Update[Robots](q)
  }

  def delete(id: ContentId): query.Query0[Unit] = {
    sql"DELETE FROM robots WHERE content_id = ${id.value}"
      .query[Unit]
  }

}
