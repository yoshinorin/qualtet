package net.yoshinorin.qualtet.domains.contentTaggings

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker
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

class DoobieContentTaggingRepository extends ContentTaggingRepository[ConnectionIO] with ConnectionIOFaker {
  override def bulkUpsert(data: List[ContentTagging]): ConnectionIO[Int] = {
    ContentTaggingQuery.bulkUpsert.updateMany(data)
  }
  override def findByTagId(id: TagId): ConnectionIO[Seq[ContentTagging]] = {
    ContentTaggingQuery.findByTagId(id).to[Seq]
  }
  override def deleteByContentId(id: ContentId): ConnectionIO[Unit] = {
    ContentTaggingQuery.deleteByContentId(id).option.map(_ => 0)
  }
  override def deleteByTagId(id: TagId): ConnectionIO[Unit] = {
    ContentTaggingQuery.deleteByTagId(id).option.map(_ => 0)
  }
  override def delete(contentId: ContentId, tagIds: Seq[TagId]): ConnectionIO[Unit] = {
    ContentTaggingQuery.delete(contentId, tagIds).option.map(_ => 0)
  }
  override def fakeRequestInt: ConnectionIO[Int] = ConnectionIOWithInt
  override def fakeRequestUnit: ConnectionIO[Unit] = ConnectionIOWithUnit
}
