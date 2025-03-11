package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.domains.contents.ContentId

trait ExternalResourceRepository[F[_]] {
  def bulkUpsert(data: List[ExternalResourceWriteModel]): F[Int]
  def findByContentId(contenId: ContentId): F[Seq[ExternalResourcesReadModel]]
  def delete(contentId: ContentId): F[Unit]
  def bulkDelete(data: List[ExternalResourceDeleteModel]): F[Unit]
  def fakeRequest(): F[Int]
  def fakeRequestUnit: F[Unit]
}

object ExternalResourceRepository {

  import cats.implicits.catsSyntaxApplicativeId
  import doobie.ConnectionIO
  import doobie.{Read, Write}

  given ExternalResourceRepository: ExternalResourceRepository[ConnectionIO] = {
    new ExternalResourceRepository[ConnectionIO] {

      given externalResourceWrite: Write[ExternalResourceWriteModel] =
        Write[(String, String, String)].contramap(p => (p.contentId.value, p.kind.value, p.name))

      given externalResourceRead: Read[ExternalResourcesReadModel] =
        Read[(String, String, String)].map { case (contentId, kind, name) =>
          ExternalResourcesReadModel(ContentId(contentId), ExternalResourceKind(kind), name)
        }

      override def bulkUpsert(data: List[ExternalResourceWriteModel]): ConnectionIO[Int] = ExternalResourceQuery.bulkUpsert.updateMany(data)

      override def findByContentId(contenId: ContentId): ConnectionIO[Seq[ExternalResourcesReadModel]] = ExternalResourceQuery.findByContentId(contenId).to[Seq]

      override def delete(contentId: ContentId): ConnectionIO[Unit] = ExternalResourceQuery.delete(contentId).run.map(_ => ())

      override def bulkDelete(data: List[ExternalResourceDeleteModel]): ConnectionIO[Unit] = ExternalResourceQuery.bulkDelete(data).run.map(_ => ())

      override def fakeRequest(): ConnectionIO[Int] = 0.pure[ConnectionIO]

      override def fakeRequestUnit: ConnectionIO[Unit] = ().pure[ConnectionIO]
    }
  }

}
