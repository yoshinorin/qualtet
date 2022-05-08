package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.robots.RepositoryRequests._

trait RobotsRepository {

  /**
   * create a robots (for meta)
   *
   * @param Upsert request object
   * @return dummy long id (Doobie return Int)
   */
  def dispatch(data: Upsert): ConnectionIO[Int]

}
