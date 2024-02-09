package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

trait ContentTaggingRepository[F[_]] {
  def bulkUpsert(data: List[ContentTagging]): F[Int]
  def findByTagId(id: TagId): F[Seq[ContentTagging]]
  def deleteByContentId(id: ContentId): F[Unit]
  def deleteByTagId(id: TagId): F[Unit]
  def delete(contentId: ContentId, tagIds: Seq[TagId]): F[Unit]
  // TODO: generics
  def fakeRequestInt: F[Int]
  def fakeRequestUnit: F[Unit]
}
