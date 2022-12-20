package net.yoshinorin.qualtet.domains.tags

import doobie.{Read, Write}
import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update
import net.yoshinorin.qualtet.domains.contents.ContentId

object TagQuery {

  def getAll(implicit responseTagRead: Read[ResponseTag]): Query0[ResponseTag] = {
    sql"SELECT * FROM tags"
      .query[ResponseTag]
  }

  def findByName(data: TagName)(implicit tagRead: Read[Tag]): Query0[Tag] = {
    sql"SELECT * FROM tags WHERE name = ${data.value}"
      .query[Tag]
  }

  def findById(id: TagId)(implicit tagRead: Read[Tag]): Query0[Tag] = {
    sql"SELECT * FROM tags WHERE id = ${id.value}"
      .query[Tag]
  }

  def findByContentId(id: ContentId)(implicit tagRead: Read[Tag]): Query0[Tag] = {
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

  def bulkUpsert(implicit tagWrite: Write[Tag]): Update[Tag] = {
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
