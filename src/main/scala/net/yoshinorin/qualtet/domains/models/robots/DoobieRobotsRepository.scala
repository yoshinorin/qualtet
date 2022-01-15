package net.yoshinorin.qualtet.domains.models.robots

import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.update.Update
import net.yoshinorin.qualtet.domains.models.contents.ContentId

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

  /**
   * find a robots by ContentId
   *
   * @param data Instance of ContentId
   * @return Robots instance
   */
  def findByContentId(data: ContentId): ConnectionIO[Option[Robots]] = {
    sql"SELECT * FROM robots WHERE content_id = $data"
      .query[Robots]
      .option
  }

}
