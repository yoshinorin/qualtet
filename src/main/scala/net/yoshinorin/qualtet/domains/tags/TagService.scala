package net.yoshinorin.qualtet.domains.tags

import cats.effect.IO
import cats.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.ServiceLogic._
import net.yoshinorin.qualtet.domains.{ServiceLogic, Continue, Done}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class TagService()(doobieContext: DoobieContextBase) {

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[ResponseTag]] = {

    def perform(): ServiceLogic[Seq[ResponseTag]] = {
      val request = GetAll()
      val resultHandler: Seq[ResponseTag] => ServiceLogic[Seq[ResponseTag]] = (resultHandler: Seq[ResponseTag]) => { Done(resultHandler) }
      Continue(request, resultHandler)
    }

    transact(perform())(doobieContext)
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {

    def perform(tagName: TagName): ServiceLogic[Option[Tag]] = {
      val request = FindByName(tagName)
      val resuleHandler: Option[Tag] => ServiceLogic[Option[Tag]] = (resuleHandler: Option[Tag]) => {
        Done(resuleHandler)
      }
      Continue(request, resuleHandler)
    }

    transact(perform(tagName))(doobieContext)
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

    def perform(data: Option[List[Tag]]): ServiceLogic[Int] = {
      val request = BulkUpsert(data)
      val resultHandler: Int => ServiceLogic[Int] = (resultHandler: Int) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    connect(perform(data))
  }
}
