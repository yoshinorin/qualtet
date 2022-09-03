package net.yoshinorin.qualtet.domains.contents

import doobie.ConnectionIO

trait ContentRepository[M[_]] {
  def upsert(data: Content): M[Int]
  def findById(id: ContentId): M[Option[Content]]
  def findByPath(path: Path): M[Option[Content]]
  def findByPathWithMeta(path: Path): M[Option[ResponseContentDbRow]]
  def delete(id: ContentId): M[Unit]
}

class DoobieContentRepository extends ContentRepository[ConnectionIO] {
  // TODO: do not `run` here
  override def upsert(data: Content): ConnectionIO[Int] = {
    ContentQuery.upsert.run(data)
  }
  override def findById(id: ContentId): ConnectionIO[Option[Content]] = {
    ContentQuery.findById(id).option
  }
  override def findByPath(path: Path): ConnectionIO[Option[Content]] = {
    ContentQuery.findByPath(path).option
  }
  override def findByPathWithMeta(path: Path): ConnectionIO[Option[ResponseContentDbRow]] = {
    ContentQuery.findByPathWithMeta(path).unique
  }
  override def delete(id: ContentId): ConnectionIO[Unit] = {
    ContentQuery.delete(id).option.map(_ => 0)
  }
}
