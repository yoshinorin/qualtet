package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.domains.contents.ContentId

trait ExternalResourceRepository[F[_]] {
  def bulkUpsert(data: List[ExternalResource]): F[Int]
  def delete(contentId: ContentId): F[Unit]
  def fakeRequest(): F[Int]
}

object ExternalResourceRepository {

  import cats.implicits.catsSyntaxApplicativeId
  import doobie.ConnectionIO
  import doobie.Write

  given ExternalResourceRepository: ExternalResourceRepository[ConnectionIO] = {
    new ExternalResourceRepository[ConnectionIO] {

      given tagWrite: Write[ExternalResource] =
        Write[(String, String, String)].contramap(p => (p.contentId.value, p.kind.value, p.name))

      override def bulkUpsert(data: List[ExternalResource]): ConnectionIO[Int] = ExternalResourceQuery.bulkUpsert.updateMany(data)
      override def delete(contentId: ContentId): ConnectionIO[Unit] = ExternalResourceQuery.delete(contentId).run.map(_ => ())
      override def fakeRequest(): ConnectionIO[Int] = 0.pure[ConnectionIO]
    }
  }

}
