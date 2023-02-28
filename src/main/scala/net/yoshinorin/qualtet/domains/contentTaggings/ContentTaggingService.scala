package net.yoshinorin.qualtet.domains.contentTaggings

import cats.effect.IO
import cats.Monad
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext
import net.yoshinorin.qualtet.syntax._

class ContentTaggingService[M[_]: Monad](
  contentTaggingRepository: ContentTaggingRepository[M]
)(dbContext: DataBaseContext[Aux[IO, Unit]]) {

  def findByTagIdActions(id: TagId): Action[Seq[ContentTagging]] = {
    Continue(contentTaggingRepository.findByTagId(id), Action.done[Seq[ContentTagging]])
  }

  def bulkUpsertActions(data: Option[List[ContentTagging]]): Action[Int] = {
    data match {
      case Some(d) => Continue(contentTaggingRepository.bulkUpsert(d), Action.done[Int])
      case None => Continue(contentTaggingRepository.fakeRequestInt, Action.done[Int])
    }
  }

  def deleteByContentIdActions(id: ContentId): Action[Unit] = {
    Continue(contentTaggingRepository.deleteByContentId(id), Action.done[Unit])
  }

  def deleteByTagIdActions(id: TagId): Action[Unit] = {
    Continue(contentTaggingRepository.deleteByTagId(id), Action.done[Unit])
  }

  def deleteActions(contentId: ContentId, tagIds: Seq[TagId]): Action[Unit] = {
    Continue(contentTaggingRepository.delete(contentId, tagIds), Action.done[Unit])
  }

  def bulkDeleteActions(data: (ContentId, Seq[TagId])): Action[Unit] = {
    data._2.size match {
      case 0 => Continue(contentTaggingRepository.fakeRequestUnit, Action.done[Unit])
      case _ => this.deleteActions(data._1, data._2)
    }
  }

  def findByTagId(id: TagId): IO[Seq[ContentTagging]] = {
    findByTagIdActions(id).perform.andTransact(dbContext)
  }
}
