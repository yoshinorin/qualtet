package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

trait ArchiveRepository[F[_]] {
  def get(contentTypeId: ContentTypeId): F[Seq[ResponseArchive]]
}
