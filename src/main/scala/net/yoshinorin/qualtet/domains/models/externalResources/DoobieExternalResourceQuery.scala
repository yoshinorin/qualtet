package net.yoshinorin.qualtet.domains.models.externalResources

import doobie.util.update.Update

object DoobieExternalResourceQuery {

  def bulkUpsert(data: List[ExternalResource]): Update[ExternalResource] = {
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

}