package net.yoshinorin.qualtet.domains.models.robots

import doobie.ConnectionIO

trait RobotsRepository {

  /**
   * create a robots (for meta)
   *
   * @param data Instance of Robots (for meta)
   * @return dummy long id (Doobie return Int)
   */
  def upsert(data: Robots): ConnectionIO[Int]

}
