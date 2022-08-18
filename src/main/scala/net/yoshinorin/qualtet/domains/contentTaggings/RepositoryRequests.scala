package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

trait ContentTaggingRepositoryRequest[T] extends RepositoryRequest[T] {
  def dispatch = ContentTaggingRepository.dispatch(this)
}
final case class BulkUpsert(data: List[ContentTagging]) extends ContentTaggingRepositoryRequest[Int]
final case class FindByTagId(id: TagId) extends ContentTaggingRepositoryRequest[Seq[ContentTagging]]
final case class DeleteByContentId(id: ContentId) extends ContentTaggingRepositoryRequest[Int]
final case class DeleteByTagId(id: TagId) extends ContentTaggingRepositoryRequest[Int]
final case class FakeRequest() extends ContentTaggingRepositoryRequest[Int]
