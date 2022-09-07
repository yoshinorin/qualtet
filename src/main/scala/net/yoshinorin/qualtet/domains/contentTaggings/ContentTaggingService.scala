package net.yoshinorin.qualtet.domains.contentTaggings

import cats.effect.IO
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.DoobieAction._
import net.yoshinorin.qualtet.domains.{DoobieAction, DoobieContinue}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class ContentTaggingService(
  contentTaggingRepository: ContentTaggingRepository[ConnectionIO]
)(doobieContext: DoobieContext) {

  def findByTagIdActions(id: TagId): DoobieAction[Seq[ContentTagging]] = {
    DoobieContinue(contentTaggingRepository.findByTagId(id), DoobieAction.buildDoneWithoutAnyHandle[Seq[ContentTagging]])
  }

  def bulkUpsertActions(data: Option[List[ContentTagging]]): DoobieAction[Int] = {
    data match {
      case Some(d) => DoobieContinue(contentTaggingRepository.bulkUpsert(d), DoobieAction.buildDoneWithoutAnyHandle[Int])
      case None => DoobieContinue(contentTaggingRepository.fakeRequestInt, DoobieAction.buildDoneWithoutAnyHandle[Int])
    }
  }

  def deleteByContentIdActions(id: ContentId): DoobieAction[Unit] = {
    DoobieContinue(contentTaggingRepository.deleteByContentId(id), DoobieAction.buildDoneWithoutAnyHandle[Unit])
  }

  def deleteByTagIdActions(id: TagId): DoobieAction[Unit] = {
    DoobieContinue(contentTaggingRepository.deleteByTagId(id), DoobieAction.buildDoneWithoutAnyHandle[Unit])
  }

  def deleteActions(contentId: ContentId, tagIds: Seq[TagId]): DoobieAction[Unit] = {
    DoobieContinue(contentTaggingRepository.delete(contentId, tagIds), DoobieAction.buildDoneWithoutAnyHandle[Unit])
  }

  def bulkDeleteActions(data: (ContentId, Seq[TagId])): DoobieAction[Unit] = {
    data._2.size match {
      case 0 => DoobieContinue(contentTaggingRepository.fakeRequestUnit, DoobieAction.buildDoneWithoutAnyHandle[Unit])
      case _ => this.deleteActions(data._1, data._2)
    }
  }

  def findByTagId(id: TagId): IO[Seq[ContentTagging]] = {
    findByTagIdActions(id).perform.andTransact(doobieContext)
  }
}
