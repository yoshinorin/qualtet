package net.yoshinorin.qualtet.domains.models.archives

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class DoobieArchiveRepository(doobie: DoobieContextBase) extends ArchiveRepository {

  def get(contentTypeId: ContentTypeId): ConnectionIO[Seq[ResponseArchive]] = {
    DoobieArchiveQuery.get(contentTypeId).to[Seq]
  }

}
