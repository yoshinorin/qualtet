package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.ContentId

class ExternalResourceService() {

  /**
   * create are ExternalResources bulky without transaction
   *
   * @param Robots instance
   * @return dummy long id (Doobie return Int)
   *
   */
  def bulkUpsertWithoutTaransact(data: Option[List[ExternalResource]]): ConnectionIO[Int] = {

    def actions(data: Option[List[ExternalResource]]): Action[Int] = {
      Continue(BulkUpsert(data), Action.buildNext[Int])
    }

    actions(data).perform
  }

  def deleteWithoutTransact(content_id: ContentId): ConnectionIO[Int] = {
    def actions(content_id: ContentId): Action[Int] = {
      // TODO: fix return type `Int` to `Unit
      Continue(Delete(content_id), Action.buildNext[Int])
    }

    actions(content_id).perform
  }
}
