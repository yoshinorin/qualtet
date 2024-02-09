package net.yoshinorin.qualtet.domains.archives

import doobie.Read
import doobie.implicits.*
import doobie.util.query.Query0
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

object ArchiveQuery {

  def get(contentTypeId: ContentTypeId)(implicit responseArhiveRead: Read[ResponseArchive]): Query0[ResponseArchive] = {
    sql"""
      SELECT path, title, published_at
      FROM contents
        WHERE content_type_id = ${contentTypeId.value}
        ORDER BY published_at desc
    """
      .query[ResponseArchive]
  }

}
