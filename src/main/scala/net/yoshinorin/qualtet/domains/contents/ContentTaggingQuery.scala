package net.yoshinorin.qualtet.domains.contents

import doobie.implicits.toSqlInterpolator
import doobie.util.update.Update
import doobie.util.query

object ContentTaggingQuery {

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

  def delete(id: ContentId): query.Query0[Unit] = {
    sql"DELETE FROM contents_tagging WHERE content_id = ${id.value}"
      .query[Unit]
  }

}
