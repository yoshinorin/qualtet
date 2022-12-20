package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

trait ContentTaggingRepository[M[_]] {
  def bulkUpsert(data: List[ContentTagging]): M[Int]
  def findByTagId(id: TagId): M[Seq[ContentTagging]]
  def deleteByContentId(id: ContentId): M[Unit]
  def deleteByTagId(id: TagId): M[Unit]
  def delete(contentId: ContentId, tagIds: Seq[TagId]): M[Unit]
  // TODO: generics
  def fakeRequestInt: M[Int]
  def fakeRequestUnit: M[Unit]
}
