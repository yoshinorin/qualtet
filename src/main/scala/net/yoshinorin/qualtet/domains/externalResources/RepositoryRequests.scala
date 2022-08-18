package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.domains.contents.ContentId

trait ExternalResourceRepositoryRequest[T] extends RepositoryRequest[T] {
  def dispatch = ExternalResourceRepository.dispatch(this)
}
final case class BulkUpsert(data: Option[List[ExternalResource]]) extends ExternalResourceRepositoryRequest[Int]
final case class Delete(content_id: ContentId) extends ExternalResourceRepositoryRequest[Int]
