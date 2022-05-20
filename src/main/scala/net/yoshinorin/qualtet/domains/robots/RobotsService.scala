package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO

class RobotsService() {

  /**
   * create a Robots without transaction
   *
   * @param Robots instance
   * @return dummy long id (Doobie return Int)
   *
   */
  def upsertWithoutTaransact(data: Robots): ConnectionIO[Int] = {

    def makeRequest(data: Robots): (Upsert, ConnectionIO[Int] => ConnectionIO[Int]) = {
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
