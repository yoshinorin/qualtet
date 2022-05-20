package net.yoshinorin.qualtet.domains.archives

import doobie.ConnectionIO

object ArchiveRepository {

  def dispatch[T](request: ArchiveRepositoryRequest[T]): ConnectionIO[T] = request match {
    case GetByContentTypeId(contentTypeId) => ArchiveQuery.get(contentTypeId).to[Seq]
  }

}
