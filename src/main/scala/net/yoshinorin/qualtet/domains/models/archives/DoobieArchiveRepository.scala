package net.yoshinorin.qualtet.domains.models.archives

import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class DoobieArchiveRepository(doobie: DoobieContextBase) extends ArchiveRepository {

  def get(contentTypeId: ContentTypeId): ConnectionIO[Seq[ResponseArchive]] = {
    sql"""
      SELECT path, title, published_at
      FROM contents
        WHERE content_type_id = $contentTypeId
        ORDER BY published_at desc
    """
      .query[ResponseArchive]
      .to[Seq]
  }

}
