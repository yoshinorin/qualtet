package net.yoshinorin.qualtet.domains.tags

import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingService
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.syntax._

class TagService(
  contentTaggingService: ContentTaggingService
)(doobieContext: DoobieContext) {

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[ResponseTag]] = {

    def actions: Action[Seq[ResponseTag]] = {
      Continue(GetAll(), Action.buildDoneWithoutAnyHandle[Seq[ResponseTag]])
    }

    actions.perform.andTransact(doobieContext)
  }

  /**
   * find tag by id
   *
   * @param id
   * @return maybe Tag
   */
  def findById(id: TagId): IO[Option[Tag]] = {

    def actions(id: TagId): Action[Option[Tag]] = {
      Continue(FindById(id), Action.buildDoneWithoutAnyHandle[Option[Tag]])
    }

    actions(id).perform.andTransact(doobieContext)
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {

    def actions(tagName: TagName): Action[Option[Tag]] = {
      Continue(FindByName(tagName), Action.buildDoneWithoutAnyHandle[Option[Tag]])
    }

    actions(tagName).perform.andTransact(doobieContext)
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
      Continue(BulkUpsert(data), Action.buildDoneWithoutAnyHandle[Int])
    }

    actions(data).perform
  }

  /**
   * delete a tag and related data by TagId
   *
   * @param id Instance of TagId
   */
  def delete(id: TagId): IO[Unit] = {

    def actions(id: TagId): Action[Int] = {
      Continue(Delete(id), Action.buildDoneWithoutAnyHandle[Int])
    }

    val queries = for {
      contentTaggingDelete <- contentTaggingService.deleteByTagIdWithoutTransaction(id)
      tagDelete <- actions(id).perform
    } yield (contentTaggingDelete, tagDelete)

    for {
      _ <- this.findById(id).throwIfNone(NotFound(s"tag not found: ${id}"))
      _ <- queries.transact(doobieContext.transactor)
    } yield ()
  }
}
