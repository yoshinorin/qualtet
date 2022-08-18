package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.repository.requests._

trait ContentRepositoryRequest[T] extends RepositoryRequest[T] {
  def dispatch = ContentRepository.dispatch(this)
}
final case class Upsert(data: Content) extends ContentRepositoryRequest[Int]
final case class FindByPath(path: Path) extends ContentRepositoryRequest[Option[Content]]
final case class FindByPathWithMeta(path: Path) extends ContentRepositoryRequest[Option[ResponseContentDbRow]]
final case class FindById(id: ContentId) extends ContentRepositoryRequest[Option[Content]]
final case class Delete(id: ContentId) extends ContentRepositoryRequest[Int]
