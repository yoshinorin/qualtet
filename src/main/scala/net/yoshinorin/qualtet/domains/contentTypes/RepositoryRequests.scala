package net.yoshinorin.qualtet.domains.contentTypes

import net.yoshinorin.qualtet.domains.repository.requests._

trait ContentTypeRepositoryRequest[T] extends RepositoryRequest[T]
final case class GetAll() extends ContentTypeRepositoryRequest[Seq[ContentType]] {
  def dispatch = ContentTypeRepository.dispatch(this)
}
final case class FindByName(name: String) extends ContentTypeRepositoryRequest[Option[ContentType]] {
  def dispatch = ContentTypeRepository.dispatch(this)
}
final case class Upsert(data: ContentType) extends ContentTypeRepositoryRequest[Int] {
  def dispatch = ContentTypeRepository.dispatch(this)
}
