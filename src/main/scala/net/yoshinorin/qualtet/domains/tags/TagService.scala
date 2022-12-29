package net.yoshinorin.qualtet.domains.tags

import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.ConnectionIO
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.utils.Action._
import net.yoshinorin.qualtet.utils.{Action, Continue}
import net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingService
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.syntax._

class TagService(
  tagRepository: TagRepository[ConnectionIO],
  contentTaggingService: ContentTaggingService
)(dbContext: DataBaseContext[Aux[IO, Unit]]) {

  def bulkUpsertActions(data: Option[List[Tag]]): Action[Int] = {
    data match {
      case Some(d) => Continue(tagRepository.bulkUpsert(d), Action.done[Int])
      case None => Continue(tagRepository.fakeRequest(), Action.done[Int])
    }
  }

  def getAllActions: Action[Seq[ResponseTag]] = {
    Continue(tagRepository.getAll(), Action.done[Seq[ResponseTag]])
  }

  def findByIdActions(id: TagId): Action[Option[Tag]] = {
    Continue(tagRepository.findById(id), Action.done[Option[Tag]])
  }

  def findByNameActions(tagName: TagName): Action[Option[Tag]] = {
    Continue(tagRepository.findByName(tagName), Action.done[Option[Tag]])
  }

  def findByContentIdActions(contenId: ContentId): Action[Seq[Tag]] = {
    Continue(tagRepository.findByContentId(contenId), Action.done[Seq[Tag]])
  }

  def deleteActions(id: TagId): Action[Unit] = {
    Continue(tagRepository.delete(id), Action.done[Unit])
  }

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[ResponseTag]] = {
    getAllActions.perform.andTransact(dbContext)
  }

  /**
   * find tag by id
   *
   * @param id
   * @return maybe Tag
   */
  def findById(id: TagId): IO[Option[Tag]] = {
    findByIdActions(id).perform.andTransact(dbContext)
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {
    findByNameActions(tagName).perform.andTransact(dbContext)
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
      _ <- queries.transact(dbContext.transactor)
    } yield ()
  }
}
