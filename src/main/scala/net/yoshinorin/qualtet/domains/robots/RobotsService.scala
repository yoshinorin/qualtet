package net.yoshinorin.qualtet.domains.robots

import cats.effect.IO
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.domains.robots.RepositoryRequests._

class RobotsService() {

  /**
   * create a Robots without transaction
   *
   * @param Robots instance
   * @return dummy long id (Doobie return Int)
   *
   */
  def upsertWithoutTaransact(data: Robots): ConnectionIO[Int] = {

    def makeRequest(data: Robots): (RepositoryRequests.Upsert, ConnectionIO[Int] => ConnectionIO[Int]) = {
      val request = Upsert(data)
      val resultHandler: ConnectionIO[Int] => ConnectionIO[Int] = (connectionIO: ConnectionIO[Int]) => { connectionIO }
      (request, resultHandler)
    }

    def run(data: Robots): ConnectionIO[Int] = {
      val (request, resultHandler) = makeRequest(data)
      RobotsRepository.dispatch(request)
    }

    run(data)
  }
}
