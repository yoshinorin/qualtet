package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker
import net.yoshinorin.qualtet.domains.contents.ContentId

trait ExternalResourceRepository[M[_]] {
  def bulkUpsert(data: List[ExternalResource]): M[Int]
  def delete(contentId: ContentId): M[Unit]
  def fakeRequest(): M[Int]
}

class DoobieExternalResourceRepository extends ExternalResourceRepository[ConnectionIO] with ConnectionIOFaker {
  override def bulkUpsert(data: List[ExternalResource]): ConnectionIO[Int] = ExternalResourceQuery.bulkUpsert.updateMany(data)
  override def delete(contentId: ContentId): ConnectionIO[Unit] = ExternalResourceQuery.delete(contentId).option.map(_ => 0)
  override def fakeRequest(): ConnectionIO[Int] = ConnectionIOWithInt
}
