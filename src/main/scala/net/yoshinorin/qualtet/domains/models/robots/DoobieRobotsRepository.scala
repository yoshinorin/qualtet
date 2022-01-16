package net.yoshinorin.qualtet.domains.models.robots

import doobie.ConnectionIO
import doobie.util.update.Update

class DoobieRobotsRepository extends RobotsRepository {

  /**
   * create a robots (for meta)
   *
   * @param data Instance of Robots (for meta)
   * @return dummy long id (Doobie return Int)
   */
  def upsert(data: Robots): ConnectionIO[Int] = {
    val q = s"""
          INSERT INTO robots (content_id, attributes)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            attributes = VALUES(attributes)
        """
    Update[Robots](q).run(data)
  }

}
