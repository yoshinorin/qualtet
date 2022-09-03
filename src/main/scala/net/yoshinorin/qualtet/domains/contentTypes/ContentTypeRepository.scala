package net.yoshinorin.qualtet.domains.contentTypes

import doobie.ConnectionIO

trait ContentTypeRepository[M[_]] {
  def upsert(data: ContentType): M[Int]
  def getAll(): M[Seq[ContentType]]
  def findByName(name: String): M[Option[ContentType]]
}

class DoobieContentTypeRepository extends ContentTypeRepository[ConnectionIO] {
  // TODO: do not `run` here
  override def upsert(data: ContentType): ConnectionIO[Int] = {
    ContentTypeQuery.upsert.run(data)
  }
  override def getAll(): ConnectionIO[Seq[ContentType]] = {
    ContentTypeQuery.getAll.to[Seq]
  }
  override def findByName(name: String): ConnectionIO[Option[ContentType]] = {
    ContentTypeQuery.findByName(name).option
  }
}
