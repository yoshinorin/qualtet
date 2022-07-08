package net.yoshinorin.qualtet.domains.tags

import cats.effect.IO
import cats.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue, Done}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class TagService()(doobieContext: DoobieContextBase) {

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[ResponseTag]] = {

    def actions(): Action[Seq[ResponseTag]] = {
      val request = GetAll()
      val resultHandler: Seq[ResponseTag] => Action[Seq[ResponseTag]] = (resultHandler: Seq[ResponseTag]) => { Done(resultHandler) }
      Continue(request, resultHandler)
    }

    actions().perform().andTransact()(doobieContext)
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {

    def actions(tagName: TagName): Action[Option[Tag]] = {
      val request = FindByName(tagName)
      val resuleHandler: Option[Tag] => Action[Option[Tag]] = (resuleHandler: Option[Tag]) => {
        Done(resuleHandler)
      }
      Continue(request, resuleHandler)
    }

    actions(tagName).perform().andTransact()(doobieContext)
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

    def actions(data: Option[List[Tag]]): Action[Int] = {
      val request = BulkUpsert(data)
      val resultHandler: Int => Action[Int] = (resultHandler: Int) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    actions(data).perform()
  }
}
