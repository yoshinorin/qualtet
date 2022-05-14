package net.yoshinorin.qualtet.domains.archives

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.archives.RepositoryReqiests._

object ArchiveRepository {

  def dispatch(request: GetByContentTypeId): ConnectionIO[Seq[ResponseArchive]] = {
    ArchiveQuery.get(request.contentTypeId).to[Seq]
  }

}
