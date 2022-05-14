package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.robots.RepositoryRequests._

object RobotsRepository {

  /**
   * create a robots (for meta)
   *
   * @param Upsert request object
   * @return dummy long id (Doobie return Int)
   */
  def dispatch(request: Upsert): ConnectionIO[Int] = {
    RobotsQuery.upsert.run(request.data)
  }

}
