package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.repository.requests._

trait TagRepositoryRequest[T] extends RepositoryRequest[T]
final case class GetAll() extends TagRepositoryRequest[Seq[ResponseTag]]
final case class FindByName(data: TagName) extends TagRepositoryRequest[Option[Tag]]
final case class BulkUpsert(data: Option[List[Tag]]) extends TagRepositoryRequest[Int]
