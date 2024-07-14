package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

trait ContentTaggingRepository[F[_]] {
  def bulkUpsert(data: List[ContentTagging]): F[Int]
  def findByTagId(id: TagId): F[Seq[ContentTagging]]
  def deleteByContentId(id: ContentId): F[Unit]
  def deleteByTagId(id: TagId): F[Unit]
  def delete(contentId: ContentId, tagIds: Seq[TagId]): F[Unit]
  // TODO: generics
  def fakeRequestInt: F[Int]
  def fakeRequestUnit: F[Unit]
}

object ContentTaggingRepository {

  import cats.implicits.catsSyntaxApplicativeId
  import doobie.{Read, Write}
  import doobie.ConnectionIO

  given ContentTaggingRepository: ContentTaggingRepository[ConnectionIO] = {
    new ContentTaggingRepository[ConnectionIO] {

      given contentTaggingRead: Read[ContentTagging] =
        Read[(String, String)].map { case (contentId, tagId) => ContentTagging(ContentId(contentId), TagId(tagId)) }

      given contentTaggingWithOptionRead: Read[Option[ContentTagging]] =
        Read[(String, String)].map { case (contentId, tagId) => Some(ContentTagging(ContentId(contentId), TagId(tagId))) }

      given contentTaggingWrite: Write[ContentTagging] =
        Write[(String, String)].contramap(c => (c.contentId.value, c.tagId.value))

      override def bulkUpsert(data: List[ContentTagging]): ConnectionIO[Int] = {
        ContentTaggingQuery.bulkUpsert.updateMany(data)
      }
      override def findByTagId(id: TagId): ConnectionIO[Seq[ContentTagging]] = {
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
      override def fakeRequestInt: ConnectionIO[Int] = 0.pure[ConnectionIO]
      override def fakeRequestUnit: ConnectionIO[Unit] = ().pure[ConnectionIO]
    }
  }

}
