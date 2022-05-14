package net.yoshinorin.qualtet.domains.archives

import doobie.implicits._
import doobie.util.query.Query0
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

object ArchiveQuery {

  def get(contentTypeId: ContentTypeId): Query0[ResponseArchive] = {
    sql"""
      SELECT path, title, published_at
      FROM contents
        WHERE content_type_id = $contentTypeId
        ORDER BY published_at desc
    """
      .query[ResponseArchive]
  }

}
