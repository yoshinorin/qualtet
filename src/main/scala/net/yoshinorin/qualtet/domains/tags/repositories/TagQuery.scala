package net.yoshinorin.qualtet.domains.tags

import doobie.{Read, Write}
import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.{Update, Update0}
import net.yoshinorin.qualtet.domains.contents.ContentId

object TagQuery {

  def getAll: Read[ResponseTag] ?=> Query0[ResponseTag] = {
    sql"""
      SELECT
        tags.id,
        tags.name,
        COUNT(*) AS count
      FROM tags
      INNER JOIN contents_tagging
        ON contents_tagging.tag_id = tags.id
      INNER JOIN contents
        ON contents_tagging.content_id = contents.id
      GROUP BY
        tags.id
      ORDER BY
        tags.name
    """
      .query[ResponseTag]
  }

  def findByName(data: TagName): Read[Tag] ?=> Query0[Tag] = {
    sql"SELECT * FROM tags WHERE name = ${data.value}"
      .query[Tag]
  }

  def findById(id: TagId): Read[Tag] ?=> Query0[Tag] = {
    sql"SELECT * FROM tags WHERE id = ${id.value}"
      .query[Tag]
  }

  def findByContentId(id: ContentId): Read[Tag] ?=> Query0[Tag] = {
    sql"""
      SELECT tags.*
      FROM
        tags
      INNER JOIN contents_tagging
        ON tags.id = contents_tagging.tag_id
      WHERE contents_tagging.content_id = ${id.value}
    """
      .query[Tag]
  }

  def bulkUpsert: Write[Tag] ?=> Update[Tag] = {
    val q = s"""
          INSERT INTO tags (id, name)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            name = VALUES(name)
        """
    Update[Tag](q)
  }

  def delete(id: TagId): Update0 = {
    sql"DELETE FROM tags WHERE id = ${id.value}".update
  }

}
