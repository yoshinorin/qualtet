package net.yoshinorin.qualtet.domains.archives

import doobie.Read
import doobie.implicits.*
import doobie.util.query.Query0
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

object ArchiveQuery {

  def get(contentTypeId: ContentTypeId): Read[ArchiveReadModel] ?=> Query0[ArchiveReadModel] = {
    sql"""
      SELECT path, title, published_at
      FROM contents
        WHERE content_type_id = ${contentTypeId.value}
        ORDER BY published_at desc
    """
      .query[ArchiveReadModel]
  }

}
