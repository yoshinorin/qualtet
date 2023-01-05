package net.yoshinorin.qualtet.domains.contentTaggings

import doobie.{Read, Write}
import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

class ContentTaggingRepositoryDoobieInterpretere extends ContentTaggingRepository[ConnectionIO] with ConnectionIOFaker {

  implicit val contentTaggingRead: Read[ContentTagging] =
    Read[(String, String)].map { case (contentId, tagId) => ContentTagging(ContentId(contentId), TagId(tagId)) }

  implicit val contentTaggingWithOptionRead: Read[Option[ContentTagging]] =
    Read[(String, String)].map { case (contentId, tagId) => Some(ContentTagging(ContentId(contentId), TagId(tagId))) }

  implicit val contentTaggingWrite: Write[ContentTagging] =
    Write[(String, String)].contramap(c => (c.contentId.value, c.tagId.value))

  override def bulkUpsert(data: List[ContentTagging]): ConnectionIO[Int] = {
    ContentTaggingQuery.bulkUpsert.updateMany(data)
  }
  override def findByTagId(id: TagId): ConnectionIO[Seq[ContentTagging]] = {
    ContentTaggingQuery.findByTagId(id).to[Seq]
  }
  override def deleteByContentId(id: ContentId): ConnectionIO[Unit] = {
    ContentTaggingQuery.deleteByContentId(id).option.map(_ => ())
  }
  override def deleteByTagId(id: TagId): ConnectionIO[Unit] = {
    ContentTaggingQuery.deleteByTagId(id).option.map(_ => ())
  }
  override def delete(contentId: ContentId, tagIds: Seq[TagId]): ConnectionIO[Unit] = {
    ContentTaggingQuery.delete(contentId, tagIds).option.map(_ => ())
  }
  override def fakeRequestInt: ConnectionIO[Int] = ConnectionIOWithInt
  override def fakeRequestUnit: ConnectionIO[Unit] = ConnectionIOWithUnit
}
