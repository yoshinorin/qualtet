package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.ContentId

class RobotsService() {

  /**
   * create a Robots without transaction
   *
   * @param Robots instance
   * @return dummy long id (Doobie return Int)
   */
  def upsertWithoutTaransact(data: Robots): ConnectionIO[Int] = {

    def actions(data: Robots): Action[Int] = {
      Continue(Upsert(data), Action.buildNext[Int])
    }

    actions(data).perform
  }

  /**
   * delete a Robots instance
   *
   * @param content_id ContentId instance
   * @return dummy id (Doobie return Int)
   */
  def deleteWithoutTransaction(content_id: ContentId): ConnectionIO[Int] = {
    def actions(content_id: ContentId): Action[Int] = {
      // TODO: fix return type `Int` to `Unit
      Continue(Delete(content_id), Action.buildNext[Int])
    }

    actions(content_id).perform
  }
}
