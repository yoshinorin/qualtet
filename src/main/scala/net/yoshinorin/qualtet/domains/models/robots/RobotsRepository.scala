package net.yoshinorin.qualtet.domains.models.robots

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contents.ContentId

trait RobotsRepository {

  /**
   * create a robots (for meta)
   *
   * @param data Instance of Robots (for meta)
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: Robots): ConnectionIO[Long]

  /**
   * find a robots by ContentId
   *
   * @param data Instance of ContentId
   * @return Robots instance
   */
  def findByContentId(data: ContentId): ConnectionIO[Option[Robots]]

}
