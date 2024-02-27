package net.yoshinorin.qualtet.domains.contentTypes

trait ContentTypeRepository[F[_]] {
  def upsert(data: ContentType): F[Int]
  def getAll(): F[Seq[ContentType]]
  def findByName(name: String): F[Option[ContentType]]
}

object ContentTypeRepository {

  import doobie.{Read, Write}
  import doobie.ConnectionIO

  given ContentTypeRepository: ContentTypeRepository[ConnectionIO] = {
    new ContentTypeRepository[ConnectionIO] {
      given contentTypeRead: Read[ContentType] =
        Read[(String, String)].map { case (id, name) => ContentType(ContentTypeId(id), name) }

      given contentTypeWithOptionRead: Read[Option[ContentType]] =
        Read[(String, String)].map { case (id, name) => Some(ContentType(ContentTypeId(id), name)) }

      given contentTypeWrite: Write[ContentType] =
        Write[(String, String)].contramap(c => (c.id.value, c.name))

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
  }
}
