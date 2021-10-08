package net.yoshinorin.qualtet.domains.models.robots

import doobie.ConnectionIO
import doobie.implicits._
import io.getquill.{idiom => _}
import net.yoshinorin.qualtet.domains.models.contents.ContentId
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieRobotsRepository(doobie: DoobieContext) extends RobotsRepository {

  import doobie.ctx._

  private val robots = quote(querySchema[Robots]("robots"))

  /**
   * create a robots (for meta)
   *
   * @param data Instance of Robots (for meta)
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: Robots): ConnectionIO[Long] = {
    val q = quote(
      robots
        .insert(lift(data))
        .onConflictUpdate((existingRow, newRow) => existingRow.attributes -> (newRow.attributes))
    )
    run(q)
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
