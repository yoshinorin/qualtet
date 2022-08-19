package net.yoshinorin.qualtet.domains.contentTaggings

import cats.effect.IO
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class ContentTaggingService()(doobieContext: DoobieContext) {

  def findByTagId(id: TagId): IO[Seq[ContentTagging]] = {
    def actions(id: TagId): Action[Seq[ContentTagging]] = {
      Continue(FindByTagId(id), Action.buildDoneWithoutAnyHandle[Seq[ContentTagging]])
    }

    actions(id).perform.andTransact(doobieContext)
  }

  def bulkUpsertWithoutTaransact(data: Option[List[ContentTagging]]): ConnectionIO[Int] = {
    def actions(data: Option[List[ContentTagging]]): Action[Int] = {
      data match {
        case Some(d) => Continue(BulkUpsert(d), Action.buildDoneWithoutAnyHandle[Int])
        case None => Continue(FakeRequest(), Action.buildDoneWithoutAnyHandle[Int])
      }

    }
    actions(data).perform
  }

  def deleteByContentIdWithoutTransaction(id: ContentId): ConnectionIO[Int] = {
    def actions(id: ContentId): Action[Int] = {
      Continue(DeleteByContentId(id), Action.buildDoneWithoutAnyHandle[Int])
    }
    actions(id).perform
  }

  def deleteByTagIdWithoutTransaction(id: TagId): ConnectionIO[Int] = {
    def actions(id: TagId): Action[Int] = {
      Continue(DeleteByTagId(id), Action.buildDoneWithoutAnyHandle[Int])
    }
    actions(id).perform
  }

}
