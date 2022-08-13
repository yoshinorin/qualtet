package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.repository.requests._

trait ContentRepositoryRequest[T] extends RepositoryRequest[T]
final case class Upsert(data: Content) extends ContentRepositoryRequest[Int] {
  def dispatch = ContentRepository.dispatch(this)
}
final case class FindByPath(path: Path) extends ContentRepositoryRequest[Option[Content]] {
  def dispatch = ContentRepository.dispatch(this)
}
final case class FindByPathWithMeta(path: Path) extends ContentRepositoryRequest[Option[ResponseContentDbRow]] {
  def dispatch = ContentRepository.dispatch(this)
}
final case class Delete(id: ContentId) extends ContentRepositoryRequest[Int] {
  def dispatch = ContentRepository.dispatch(this)
}
