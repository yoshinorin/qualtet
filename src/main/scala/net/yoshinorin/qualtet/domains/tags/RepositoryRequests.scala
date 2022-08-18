package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.repository.requests._

trait TagRepositoryRequest[T] extends RepositoryRequest[T] {
  def dispatch = TagRepository.dispatch(this)
}
final case class GetAll() extends TagRepositoryRequest[Seq[ResponseTag]]
final case class FindById(id: TagId) extends TagRepositoryRequest[Option[Tag]]
final case class FindByName(data: TagName) extends TagRepositoryRequest[Option[Tag]]
final case class BulkUpsert(data: Option[List[Tag]]) extends TagRepositoryRequest[Int]
final case class Delete(id: TagId) extends TagRepositoryRequest[Int]
