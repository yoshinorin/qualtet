package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.contents.ContentId

trait TagRepository[F[_]] {
  def bulkUpsert(data: List[Tag]): F[Int]
  def getAll(): F[Seq[TagWithCountReadModel]]
  def findById(id: TagId): F[Option[TagReadModel]]
  def findByName(id: TagName): F[Option[TagReadModel]]
  def findByContentId(conetntId: ContentId): F[Seq[TagReadModel]]
  def delete(id: TagId): F[Unit]
  def fakeRequest(): F[Int]
}

object TagRepository {

  import cats.implicits.catsSyntaxApplicativeId
  import doobie.{Read, Write}
  import doobie.ConnectionIO

  given TagRepository: TagRepository[ConnectionIO] = {
    new TagRepository[ConnectionIO] {

      given responseTagRead: Read[TagWithCountReadModel] =
        Read[(String, String, Int)].map { case (id, name, count) => TagWithCountReadModel(TagId(id), TagName(name), count) }

      given responseTagReadWithOption: Read[Option[TagWithCountReadModel]] =
        Read[(String, String, Int)].map { case (id, name, count) => Some(TagWithCountReadModel(TagId(id), TagName(name), count)) }

      given tagRead: Read[TagReadModel] =
        Read[(String, String)].map { case (id, name) => TagReadModel(TagId(id), TagName(name)) }

      given tagReadWithOption: Read[Option[TagReadModel]] =
        Read[(String, String)].map { case (id, name) => Some(TagReadModel(TagId(id), TagName(name))) }

      given tagWrite: Write[Tag] =
        Write[(String, String)].contramap(p => (p.id.value, p.name.value))

      override def bulkUpsert(data: List[Tag]): ConnectionIO[Int] = {
        TagQuery.bulkUpsert.updateMany(data)
      }
      override def getAll(): ConnectionIO[Seq[TagWithCountReadModel]] = {
        TagQuery.getAll.to[Seq]
      }
      override def findById(id: TagId): ConnectionIO[Option[TagReadModel]] = {
        TagQuery.findById(id).option
      }
      override def findByName(name: TagName): ConnectionIO[Option[TagReadModel]] = {
        TagQuery.findByName(name).option
      }
      override def findByContentId(conetntId: ContentId): ConnectionIO[Seq[TagReadModel]] = {
        TagQuery.findByContentId(conetntId).to[Seq]
      }
      override def delete(id: TagId): ConnectionIO[Unit] = {
        TagQuery.delete(id).run.map(_ => ())
      }
      override def fakeRequest(): ConnectionIO[Int] = 0.pure[ConnectionIO]

    }
  }

}
