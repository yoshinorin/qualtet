package net.yoshinorin.qualtet.domains.contents

import doobie.util.update.Update

object DoobieContentTaggingQuery {

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

}
