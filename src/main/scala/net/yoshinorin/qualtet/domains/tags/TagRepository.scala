package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.contents.ContentId

trait TagRepository[M[_]] {
  def bulkUpsert(data: List[Tag]): M[Int]
  def getAll(): M[Seq[ResponseTag]]
  def findById(id: TagId): M[Option[Tag]]
  def findByName(id: TagName): M[Option[Tag]]
  def findByContentId(conetntId: ContentId): M[Seq[Tag]]
  def delete(id: TagId): M[Unit]
  def fakeRequest(): M[Int]
}
