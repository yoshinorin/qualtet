package net.yoshinorin.qualtet.domains.authors

import net.yoshinorin.qualtet.domains.repository.requests._

trait AuthorRepositoryRequest[T] extends RepositoryRequest[T]
final case class GetAll() extends AuthorRepositoryRequest[Seq[ResponseAuthor]]
final case class FindById(id: AuthorId) extends AuthorRepositoryRequest[Option[ResponseAuthor]]
final case class FindByIdWithPassword(id: AuthorId) extends AuthorRepositoryRequest[Option[Author]]
final case class FindByName(name: AuthorName) extends AuthorRepositoryRequest[Option[ResponseAuthor]]
final case class Upsert(data: Author) extends AuthorRepositoryRequest[Int]
