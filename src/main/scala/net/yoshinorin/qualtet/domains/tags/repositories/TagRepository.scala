package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.contents.ContentId

trait TagRepository[F[_]] {
  def bulkUpsert(data: List[TagWriteModel]): F[Int]
  def getAll(): F[Seq[(Int, TagReadModel)]]
  def findById(id: TagId): F[Option[TagReadModel]]
  def findByName(id: TagName): F[Option[TagReadModel]]
  def findByContentId(conetntId: ContentId): F[Seq[TagReadModel]]
  def delete(id: TagId): F[Unit]
}

object TagRepository {

  import doobie.{Read, Write}
  import doobie.ConnectionIO

  given TagRepository: TagRepository[ConnectionIO] = {
    new TagRepository[ConnectionIO] {

      given tagWithCountRead: Read[(Int, TagReadModel)] =
        Read[(Int, (String, String))].map { case (count, (id, name)) => (count, TagReadModel(TagId(id), TagName(name))) }

      given tagRead: Read[TagReadModel] =
        Read[(String, String)].map { case (id, name) => TagReadModel(TagId(id), TagName(name)) }

      given tagOrOptionRead: Read[Option[TagReadModel]] =
        Read[(String, String)].map { case (id, name) => Some(TagReadModel(TagId(id), TagName(name))) }

      given tagWrite: Write[TagWriteModel] =
        Write[(String, String)].contramap(p => (p.id.value, p.name.value))

      override def bulkUpsert(data: List[TagWriteModel]): ConnectionIO[Int] = {
        TagQuery.bulkUpsert.updateMany(data)
      }
      override def getAll(): ConnectionIO[Seq[(Int, TagReadModel)]] = {
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
    }
  }

}
