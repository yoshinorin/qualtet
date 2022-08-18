package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

trait ContentTaggingRepositoryRequest[T] extends RepositoryRequest[T]
final case class BulkUpsert(data: List[ContentTagging]) extends ContentTaggingRepositoryRequest[Int] {
  def dispatch = ContentTaggingRepository.dispatch(this)
}
final case class FindByTagId(id: TagId) extends ContentTaggingRepositoryRequest[Seq[ContentTagging]] {
  def dispatch = ContentTaggingRepository.dispatch(this)
}
final case class DeleteByContentId(id: ContentId) extends ContentTaggingRepositoryRequest[Int] {
  def dispatch = ContentTaggingRepository.dispatch(this)
}
final case class DeleteByTagId(id: TagId) extends ContentTaggingRepositoryRequest[Int] {
  def dispatch = ContentTaggingRepository.dispatch(this)
}
final case class FakeRequest() extends ContentTaggingRepositoryRequest[Int] {
  def dispatch = ContentTaggingRepository.dispatch(this)
}
