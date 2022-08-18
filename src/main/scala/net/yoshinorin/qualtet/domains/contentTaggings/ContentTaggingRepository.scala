package net.yoshinorin.qualtet.domains.contentTaggings

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

object ContentTaggingRepository extends ConnectionIOFaker {

  def findByTagId(id: TagId): ConnectionIO[Seq[ContentTagging]] = {
    // TODO: work around
    ContentTaggingQuery.findByTagId(id).to[Seq]
  }

  /**
   * create a ContentTagging bulky
   *
   * @param data List of ContentTagging
   * @return dummy long id (Doobie return Int)
   *
   * TODO: remove Option
   * TODO: return ConnectionIO[Long]
   */
  def bulkUpsert(data: Option[List[ContentTagging]]): ConnectionIO[Int] = {
    data match {
      case None => ConnectionIOWithInt
      case Some(x) =>
        ContentTaggingQuery.bulkUpsert.updateMany(x)
    }
  }

  def deleteByContentId(id: ContentId): ConnectionIO[Int] = {
    // TODO: work around
    ContentTaggingQuery.deleteByContentId(id).option.map(_ => 0)
  }

  def deleteByTagId(id: TagId): ConnectionIO[Int] = {
    // TODO: work around
    ContentTaggingQuery.deleteByTagId(id).option.map(_ => 0)
  }

}
