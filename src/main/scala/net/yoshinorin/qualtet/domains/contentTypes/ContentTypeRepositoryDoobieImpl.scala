package net.yoshinorin.qualtet.domains.contentTypes

import doobie.{Read, Write}
import doobie.ConnectionIO

class ContentTypeRepositoryDoobieImpl extends ContentTypeRepository[ConnectionIO] {

  implicit val contentTypeRead: Read[ContentType] =
    Read[(String, String)].map { case (id, name) => ContentType(ContentTypeId(id), name) }

  implicit val contentTypeWithOptionRead: Read[Option[ContentType]] =
    Read[(String, String)].map { case (id, name) => Some(ContentType(ContentTypeId(id), name)) }

  implicit val contentTypeWrite: Write[ContentType] =
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
