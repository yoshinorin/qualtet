package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO

class DoobieRobotsRepository extends RobotsRepository {

  /**
   * create a robots (for meta)
   *
   * @param data Instance of Robots (for meta)
   * @return dummy long id (Doobie return Int)
   */
  def upsert(data: Robots): ConnectionIO[Int] = {
    DoobieRobotsQuery.upsert.run(data)
  }

}
