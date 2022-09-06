package net.yoshinorin.qualtet.domains.tags

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker
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

class DoobieTagRepository extends TagRepository[ConnectionIO] with ConnectionIOFaker {
  override def bulkUpsert(data: List[Tag]): ConnectionIO[Int] = {
    TagQuery.bulkUpsert.updateMany(data)
  }
  override def getAll(): ConnectionIO[Seq[ResponseTag]] = {
    TagQuery.getAll.to[Seq]
  }
  override def findById(id: TagId): ConnectionIO[Option[Tag]] = {
    TagQuery.findById(id).option
  }
  override def findByName(name: TagName): ConnectionIO[Option[Tag]] = {
    TagQuery.findByName(name).option
  }
  override def findByContentId(conetntId: ContentId): ConnectionIO[Seq[Tag]] = {
    TagQuery.findByContentId(conetntId).to[Seq]
  }
  override def delete(id: TagId): ConnectionIO[Unit] = {
    TagQuery.delete(id).option.map(_ => 0)
  }
  override def fakeRequest(): ConnectionIO[Int] = ConnectionIOWithInt
}
