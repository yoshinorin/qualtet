package net.yoshinorin.qualtet.domains.archives

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.archives.RepositoryReqiests._

class DoobieArchiveRepository extends ArchiveRepository {

  def dispatch(request: GetByContentTypeId): ConnectionIO[Seq[ResponseArchive]] = {
    DoobieArchiveQuery.get(request.contentTypeId).to[Seq]
  }

}
