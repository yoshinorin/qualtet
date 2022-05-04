package net.yoshinorin.qualtet.domains.archives

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

class DoobieArchiveRepository extends ArchiveRepository {

  def get(contentTypeId: ContentTypeId): ConnectionIO[Seq[ResponseArchive]] = {
    DoobieArchiveQuery.get(contentTypeId).to[Seq]
  }

}
