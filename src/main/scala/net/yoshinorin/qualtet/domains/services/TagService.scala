package net.yoshinorin.qualtet.domains.services

import cats.implicits._
import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.tags.{ResponseTag, Tag, TagId, TagName, TagRepository}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class TagService(
  tagRepository: TagRepository
)(
  implicit doobieContext: DoobieContextBase
) {

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[ResponseTag]] = {
    tagRepository.getAll.transact(doobieContext.transactor)
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {
    tagRepository.findByName(tagName).transact(doobieContext.transactor)
  }

  /**
   * find tag from db or create new instance (just create instance, no insert to DB)
   *
   * @param tagName
   * @return Tag instance
   */
  def findOrGetNewInstance(tagName: TagName): IO[Tag] = {
    this.findByName(tagName).flatMap {
      case None => IO(Tag(new TagId, tagName))
      case Some(t) => IO(t)
    }
  }

  /**
   * get tag from db or new instance
   *
   * @param tagNames
   * @return
   */
  def getTags(tagNames: Option[List[String]]): IO[Option[List[Tag]]] = {
    tagNames match {
      case None => IO(None)
      case Some(t) => t.map { t => findOrGetNewInstance(TagName(t)) }.sequence.option
    }
  }

  /**
   * create a Tag
   *
   * @param data List of Tag
   * @return dummy long id (Doobie return Int)
   *
   * TODO: avoid using ConnectionIO
   */
  def bulkUpsertWithoutTaransact(data: Option[List[Tag]]): ConnectionIO[Int] = {
    tagRepository.bulkUpsert(data)
  }
}
