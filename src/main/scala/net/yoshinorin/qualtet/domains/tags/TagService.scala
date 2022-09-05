package net.yoshinorin.qualtet.domains.tags

import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.DoobieAction._
import net.yoshinorin.qualtet.domains.{DoobieAction, DoobieContinue}
import net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingService
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.syntax._

class TagService(
  tagRepository: TagRepository[ConnectionIO],
  contentTaggingService: ContentTaggingService
)(doobieContext: DoobieContext) {

  def bulkUpsertActions(data: Option[List[Tag]]): DoobieAction[Int] = {
    data match {
      case Some(d) => DoobieContinue(tagRepository.bulkUpsert(d), DoobieAction.buildDoneWithoutAnyHandle[Int])
      case None => DoobieContinue(tagRepository.fakeRequest(), DoobieAction.buildDoneWithoutAnyHandle[Int])
    }
  }

  def getAllActions: DoobieAction[Seq[ResponseTag]] = {
    DoobieContinue(tagRepository.getAll(), DoobieAction.buildDoneWithoutAnyHandle[Seq[ResponseTag]])
  }

  def findByIdActions(id: TagId): DoobieAction[Option[Tag]] = {
    DoobieContinue(tagRepository.findById(id), DoobieAction.buildDoneWithoutAnyHandle[Option[Tag]])
  }

  def findByNameActions(tagName: TagName): DoobieAction[Option[Tag]] = {
    DoobieContinue(tagRepository.findByName(tagName), DoobieAction.buildDoneWithoutAnyHandle[Option[Tag]])
  }

  def deleteActions(id: TagId): DoobieAction[Unit] = {
    DoobieContinue(tagRepository.delete(id), DoobieAction.buildDoneWithoutAnyHandle[Unit])
  }

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[ResponseTag]] = {
    getAllActions.perform.andTransact(doobieContext)
  }

  /**
   * find tag by id
   *
   * @param id
   * @return maybe Tag
   */
  def findById(id: TagId): IO[Option[Tag]] = {
    findByIdActions(id).perform.andTransact(doobieContext)
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {
    findByNameActions(tagName).perform.andTransact(doobieContext)
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
   * delete a tag and related data by TagId
   *
   * @param id Instance of TagId
   */
  def delete(id: TagId): IO[Unit] = {
    val queries = for {
      contentTaggingDelete <- contentTaggingService.deleteByTagIdActions(id).perform
      tagDelete <- deleteActions(id).perform
    } yield (contentTaggingDelete, tagDelete)

    for {
      _ <- this.findById(id).throwIfNone(NotFound(s"tag not found: ${id}"))
      _ <- queries.transact(doobieContext.transactor)
    } yield ()
  }
}
