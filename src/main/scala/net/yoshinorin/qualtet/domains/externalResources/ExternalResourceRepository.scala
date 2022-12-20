package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import doobie.Write
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker
import net.yoshinorin.qualtet.domains.contents.ContentId

trait ExternalResourceRepository[M[_]] {
  def bulkUpsert(data: List[ExternalResource]): M[Int]
  def delete(contentId: ContentId): M[Unit]
  def fakeRequest(): M[Int]
}

class DoobieExternalResourceRepository extends ExternalResourceRepository[ConnectionIO] with ConnectionIOFaker {

  implicit val tagWrite: Write[ExternalResource] =
    Write[(String, String, String)].contramap(p => (p.contentId.value, p.kind.value, p.name))

  override def bulkUpsert(data: List[ExternalResource]): ConnectionIO[Int] = ExternalResourceQuery.bulkUpsert.updateMany(data)
  override def delete(contentId: ContentId): ConnectionIO[Unit] = ExternalResourceQuery.delete(contentId).option.map(_ => ())
  override def fakeRequest(): ConnectionIO[Int] = ConnectionIOWithInt
}
