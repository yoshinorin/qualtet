package net.yoshinorin.qualtet.domains.archives

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

trait ArchiveRepository[M[_]] {
  def get(contentTypeId: ContentTypeId): M[Seq[ResponseArchive]]
}

class DoobieArchiveRepository extends ArchiveRepository[ConnectionIO] {
  override def get(contentTypeId: ContentTypeId): ConnectionIO[Seq[ResponseArchive]] = ArchiveQuery.get(contentTypeId).to[Seq]
}
