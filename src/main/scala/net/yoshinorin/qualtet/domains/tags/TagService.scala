package net.yoshinorin.qualtet.domains.tags

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.actions.Action.*
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingService
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.syntax.*

class TagService[F[_]: Monad](
  tagRepository: TagRepository[F],
  contentTaggingService: ContentTaggingService[F]
)(using transactor: Executer[F, IO]) {

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
    transactor.transact(getAllActions)
  }

  /**
   * find tag by id
   *
   * @param id
   * @return maybe Tag
   */
  def findById(id: TagId): IO[Option[Tag]] = {
    transactor.transact(findByIdActions(id))
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {
    transactor.transact(findByNameActions(tagName))
  }

  /**
   * find tag from db or create new instance (just create instance, no insert to DB)
   *
   * @param tagName
   * @return Tag instance
   */
  def findOrGetNewInstance(tagName: TagName): IO[Tag] = {
    this.findByName(tagName).flatMap {
      case None => IO(Tag(TagId.apply(), tagName))
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
      contentTaggingDelete <- transactor.perform(contentTaggingService.deleteByTagIdActions(id))
      tagDelete <- transactor.perform(deleteActions(id))
    } yield (contentTaggingDelete, tagDelete)

    for {
      _ <- this.findById(id).throwIfNone(NotFound(detail = s"tag not found: ${id}"))
      _ <- transactor.transact2(queries)
    } yield ()
  }
}
