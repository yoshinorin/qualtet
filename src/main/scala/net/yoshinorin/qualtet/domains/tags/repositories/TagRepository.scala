package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.contents.ContentId

trait TagRepository[F[_]] {
  def bulkUpsert(data: List[Tag]): F[Int]
  def getAll(): F[Seq[ResponseTag]]
  def findById(id: TagId): F[Option[Tag]]
  def findByName(id: TagName): F[Option[Tag]]
  def findByContentId(conetntId: ContentId): F[Seq[Tag]]
  def delete(id: TagId): F[Unit]
  def fakeRequest(): F[Int]
}
