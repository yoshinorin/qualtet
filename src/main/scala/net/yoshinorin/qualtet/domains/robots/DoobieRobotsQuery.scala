package net.yoshinorin.qualtet.domains.robots

import doobie.util.update.Update

object DoobieRobotsQuery {

  def upsert: Update[Robots] = {
    val q = s"""
          INSERT INTO robots (content_id, attributes)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            attributes = VALUES(attributes)
        """
    Update[Robots](q)
  }

}
