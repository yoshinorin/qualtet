package net.yoshinorin.qualtet.domains.contentTypes

trait ContentTypeRepository[F[_]] {
  def upsert(data: ContentTypeWriteModel): F[Int]
  def getAll(): F[Seq[ContentTypeReadModel]]
  def findByName(name: ContentTypeName): F[Option[ContentTypeReadModel]]
}

object ContentTypeRepository {

  import doobie.{Read, Write}
  import doobie.ConnectionIO

  given ContentTypeRepository: ContentTypeRepository[ConnectionIO] = {
    new ContentTypeRepository[ConnectionIO] {
      given contentTypeRead: Read[ContentTypeReadModel] =
        Read[(String, String)].map { case (id, name) =>
          ContentTypeReadModel(
            ContentTypeId(id),
            ContentTypeName.fromTrusted(name)
          )
        }

      given contentTypeOrOptionRead: Read[Option[ContentTypeReadModel]] =
        Read[(String, String)].map { case (id, name) =>
          Some(
            ContentTypeReadModel(
              ContentTypeId(id),
              ContentTypeName.fromTrusted(name)
            )
          )
        }

      given contentTypeWrite: Write[ContentTypeWriteModel] =
        Write[(String, String)].contramap(c => (c.id.value, c.name.value))

      // TODO: do not `run` here
      override def upsert(data: ContentTypeWriteModel): ConnectionIO[Int] = {
        ContentTypeQuery.upsert.run(data)
      }
      override def getAll(): ConnectionIO[Seq[ContentTypeReadModel]] = {
        ContentTypeQuery.getAll.to[Seq]
      }
      override def findByName(name: ContentTypeName): ConnectionIO[Option[ContentTypeReadModel]] = {
        ContentTypeQuery.findByName(name).option
      }
    }
  }
}
