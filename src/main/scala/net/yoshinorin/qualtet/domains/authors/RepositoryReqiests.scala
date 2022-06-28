package net.yoshinorin.qualtet.domains.authors

import net.yoshinorin.qualtet.domains.repository.requests._

trait AuthorRepositoryRequest[T] extends RepositoryRequest[T]
final case class GetAll() extends AuthorRepositoryRequest[Seq[ResponseAuthor]] {
  def dispatch = AuthorRepository.dispatch(this)
}
final case class FindById(id: AuthorId) extends AuthorRepositoryRequest[Option[ResponseAuthor]] {
  def dispatch = AuthorRepository.dispatch(this)
}
final case class FindByIdWithPassword(id: AuthorId) extends AuthorRepositoryRequest[Option[Author]] {
  def dispatch = AuthorRepository.dispatch(this)
}
final case class FindByName(name: AuthorName) extends AuthorRepositoryRequest[Option[ResponseAuthor]] {
  def dispatch = AuthorRepository.dispatch(this)
}
final case class Upsert(data: Author) extends AuthorRepositoryRequest[Int] {
  def dispatch = AuthorRepository.dispatch(this)
}
