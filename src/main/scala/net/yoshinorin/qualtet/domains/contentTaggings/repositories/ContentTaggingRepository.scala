package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

trait ContentTaggingRepository[F[_]] {
  def bulkUpsert(data: List[ContentTaggingWriteModel]): F[Int]
  def findByTagId(id: TagId): F[Seq[ContentTaggingReadModel]]
  def deleteByContentId(id: ContentId): F[Unit]
  def deleteByTagId(id: TagId): F[Unit]
  def delete(contentId: ContentId, tagIds: Seq[TagId]): F[Unit]
}

object ContentTaggingRepository {

  import doobie.{Read, Write}
  import doobie.ConnectionIO

  given ContentTaggingRepository: ContentTaggingRepository[ConnectionIO] = {
    new ContentTaggingRepository[ConnectionIO] {

      given contentTaggingRead: Read[ContentTaggingReadModel] =
        Read[(String, String)].map { case (contentId, tagId) => ContentTaggingReadModel(ContentId(contentId), TagId(tagId)) }

      given contentTaggingOrOptionRead: Read[Option[ContentTaggingReadModel]] =
        Read[(String, String)].map { case (contentId, tagId) => Some(ContentTaggingReadModel(ContentId(contentId), TagId(tagId))) }

      given contentTaggingWrite: Write[ContentTaggingWriteModel] =
        Write[(String, String)].contramap(c => (c.contentId.value, c.tagId.value))

      override def bulkUpsert(data: List[ContentTaggingWriteModel]): ConnectionIO[Int] = {
        ContentTaggingQuery.bulkUpsert.updateMany(data)
      }
      override def findByTagId(id: TagId): ConnectionIO[Seq[ContentTaggingReadModel]] = {
        ContentTaggingQuery.findByTagId(id).to[Seq]
      }
      override def deleteByContentId(id: ContentId): ConnectionIO[Unit] = {
        ContentTaggingQuery.deleteByContentId(id).run.map(_ => ())
      }
      override def deleteByTagId(id: TagId): ConnectionIO[Unit] = {
        ContentTaggingQuery.deleteByTagId(id).run.map(_ => ())
      }
      override def delete(contentId: ContentId, tagIds: Seq[TagId]): ConnectionIO[Unit] = {
        ContentTaggingQuery.delete(contentId, tagIds).run.map(_ => ())
      }
    }
  }

}
