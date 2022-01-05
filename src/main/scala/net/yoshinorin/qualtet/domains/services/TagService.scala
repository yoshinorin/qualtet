package net.yoshinorin.qualtet.domains.services

import cats.implicits._
import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.tags.{Tag, TagId, TagName, TagRepository}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class TagService(
  tagRepository: TagRepository
)(
  implicit doobieContext: DoobieContext
) {

  // TODO: to generics & move somewhere
  def toTag(k: Option[String], v: Option[String]): Option[List[Tag]] = {
    val keys = k match {
      case None => return None
      case Some(x) => x.split(",").map(_.trim).toList
    }
    val values = v match {
      case None => return None
      case Some(x) => x.split(",").map(_.trim).toList
    }
    if (keys.size =!= values.size) {
      return None
    }
    Option(keys.zip(values).map(x => Tag(TagId(x._1), TagName(x._2))))
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
   * TODO: remove Option
   * TODO: return ConnectionIO[Long]
   * TODO: avoid using ConnectionIO
   */
  def bulkUpsertWithoutTaransact(data: Option[List[Tag]]): ConnectionIO[Int] = {
    tagRepository.bulkUpsert(data)
  }
}
