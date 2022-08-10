package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}

class RobotsService() {

  /**
   * create a Robots without transaction
   *
   * @param Robots instance
   * @return dummy long id (Doobie return Int)
   *
   */
  def upsertWithoutTaransact(data: Robots): ConnectionIO[Int] = {

    def actions(data: Robots): Action[Int] = {
      Continue(Upsert(data), Action.buildNext[Int])
    }

    actions(data).perform
  }
}
