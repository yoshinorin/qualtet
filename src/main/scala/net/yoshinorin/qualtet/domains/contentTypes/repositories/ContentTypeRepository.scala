package net.yoshinorin.qualtet.domains.contentTypes

trait ContentTypeRepository[F[_]] {
  def upsert(data: ContentTypeWriteModel): F[Int]
  def getAll(): F[Seq[ContentTypeReadModel]]
  def findByName(name: ContentTypeName): F[Option[ContentTypeReadModel]]
}

object ContentTypeRepository {

  import org.typelevel.doobie.{ConnectionIO, Get, Read, Write}

  given ContentTypeRepository: ContentTypeRepository[ConnectionIO] = {
    new ContentTypeRepository[ConnectionIO] {
      given contentTypeNameGet: Get[ContentTypeName] = Get[String].map(ContentTypeName.fromTrusted)

      given contentTypeRead: Read[ContentTypeReadModel] =
        Read[(String, ContentTypeName)].map { case (id, name) =>
          ContentTypeReadModel(
            ContentTypeId(id),
            name
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
