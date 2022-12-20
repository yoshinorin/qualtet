package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

trait ArchiveRepository[M[_]] {
  def get(contentTypeId: ContentTypeId): M[Seq[ResponseArchive]]
}
