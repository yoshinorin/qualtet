package net.yoshinorin.qualtet.domains.tags

import doobie.{Read, Write}
import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker
import net.yoshinorin.qualtet.domains.contents.ContentId

class TagRepositoryDoobieImpl extends TagRepository[ConnectionIO] with ConnectionIOFaker {

  implicit val responseTagRead: Read[ResponseTag] =
    Read[(String, String)].map { case (id, name) => ResponseTag(TagId(id), TagName(name)) }

  implicit val responseTagReadWithOption: Read[Option[ResponseTag]] =
    Read[(String, String)].map { case (id, name) => Some(ResponseTag(TagId(id), TagName(name))) }

  implicit val tagRead: Read[Tag] =
    Read[(String, String)].map { case (id, name) => Tag(TagId(id), TagName(name)) }

  implicit val tagReadWithOption: Read[Option[Tag]] =
    Read[(String, String)].map { case (id, name) => Some(Tag(TagId(id), TagName(name))) }

  implicit val tagWrite: Write[Tag] =
    Write[(String, String)].contramap(p => (p.id.value, p.name.value))

  override def bulkUpsert(data: List[Tag]): ConnectionIO[Int] = {
    TagQuery.bulkUpsert.updateMany(data)
  }
  override def getAll(): ConnectionIO[Seq[ResponseTag]] = {
    TagQuery.getAll.to[Seq]
  }
  override def findById(id: TagId): ConnectionIO[Option[Tag]] = {
    TagQuery.findById(id).option
  }
  override def findByName(name: TagName): ConnectionIO[Option[Tag]] = {
    TagQuery.findByName(name).option
  }
  override def findByContentId(conetntId: ContentId): ConnectionIO[Seq[Tag]] = {
    TagQuery.findByContentId(conetntId).to[Seq]
  }
  override def delete(id: TagId): ConnectionIO[Unit] = {
    TagQuery.delete(id).option.map(_ => ())
  }
  override def fakeRequest(): ConnectionIO[Int] = ConnectionIOWithInt
}
