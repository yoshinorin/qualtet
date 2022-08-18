package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

trait ArchiveRepositoryRequest[T] extends RepositoryRequest[T] {
  def dispatch = ArchiveRepository.dispatch(this)
}
final case class GetByContentTypeId(contentTypeId: ContentTypeId) extends ArchiveRepositoryRequest[Seq[ResponseArchive]]
