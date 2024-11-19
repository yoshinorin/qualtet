package net.yoshinorin.qualtet.domains.tags

import doobie.{Read, Write}
import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.{Update, Update0}
import net.yoshinorin.qualtet.domains.contents.ContentId

object TagQuery {

  def getAll: Read[TagWithCountReadModel] ?=> Query0[TagWithCountReadModel] = {
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
      .query[TagWithCountReadModel]
  }

  def findByName(data: TagName): Read[TagReadModel] ?=> Query0[TagReadModel] = {
    sql"SELECT * FROM tags WHERE name = ${data.value}"
      .query[TagReadModel]
  }

  def findById(id: TagId): Read[TagReadModel] ?=> Query0[TagReadModel] = {
    sql"SELECT * FROM tags WHERE id = ${id.value}"
      .query[TagReadModel]
  }

  def findByContentId(id: ContentId): Read[TagReadModel] ?=> Query0[TagReadModel] = {
    sql"""
      SELECT tags.*
      FROM
        tags
      INNER JOIN contents_tagging
        ON tags.id = contents_tagging.tag_id
      WHERE contents_tagging.content_id = ${id.value}
    """
      .query[TagReadModel]
  }

  def bulkUpsert: Write[TagWriteModel] ?=> Update[TagWriteModel] = {
    val q = s"""
          INSERT INTO tags (id, name)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            name = VALUES(name)
        """
    Update[TagWriteModel](q)
  }

  def delete(id: TagId): Update0 = {
    sql"DELETE FROM tags WHERE id = ${id.value}".update
  }

}
