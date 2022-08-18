package net.yoshinorin.qualtet.domains.contentTaggings

import doobie.implicits.toSqlInterpolator
import doobie.util.update.Update
import doobie.util.query
import net.yoshinorin.qualtet.domains.tags.TagId
import net.yoshinorin.qualtet.domains.contents.ContentId

object ContentTaggingQuery {

  def findByTagId(id: TagId): query.Query0[ContentTagging] = {
    sql"SELECT * FROM contents_tagging FROM tag_id = ${id.value}"
      .query[ContentTagging]
  }

  def bulkUpsert: Update[ContentTagging] = {
    val q = s"""
          INSERT INTO contents_tagging (content_id, tag_id)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            content_id = VALUES(content_id),
            tag_id = VALUES(tag_id)
        """
    Update[ContentTagging](q)
  }

  def deleteByContentId(id: ContentId): query.Query0[Unit] = {
    sql"DELETE FROM contents_tagging WHERE content_id = ${id.value}"
      .query[Unit]
  }

  def deleteByTagId(id: TagId): query.Query0[Unit] = {
    sql"DELETE FROM contents_tagging WHERE tag_id = ${id.value}"
      .query[Unit]
  }

}
