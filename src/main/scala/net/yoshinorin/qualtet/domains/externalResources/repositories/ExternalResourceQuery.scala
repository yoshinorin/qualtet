package net.yoshinorin.qualtet.domains.externalResources

import doobie.Write
import doobie.implicits.toSqlInterpolator
import doobie.util.update.{Update, Update0}
import net.yoshinorin.qualtet.domains.contents.ContentId

object ExternalResourceQuery {

  def bulkUpsert: Write[ExternalResource] ?=> Update[ExternalResource] = {
    val q = s"""
          INSERT INTO external_resources (content_id, kind, name)
            VALUES (?, ?, ?)
          ON DUPLICATE KEY UPDATE
            content_id = VALUES(content_id),
            kind = VALUES(kind),
            name = VALUES(name)
        """
    Update[ExternalResource](q)
  }

  def delete(id: ContentId): Update0 = {
    sql"DELETE FROM external_resources WHERE content_id = ${id.value}".update
  }

}
