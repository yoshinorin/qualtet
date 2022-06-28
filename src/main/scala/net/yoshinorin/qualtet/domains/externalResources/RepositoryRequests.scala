package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.domains.repository.requests._

trait ExternalResourceRepositoryRequest[T] extends RepositoryRequest[T]
final case class BulkUpsert(data: Option[List[ExternalResource]]) extends ExternalResourceRepositoryRequest[Int] {
  def dispatch = ExternalResourceRepository.dispatch(this)
}
