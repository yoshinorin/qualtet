package net.yoshinorin.qualtet.domains.models.tags

import doobie.ConnectionIO
import doobie.util.update.Update
import net.yoshinorin.qualtet.infrastructure.db.doobie.{ConnectionIOFaker, DoobieContext}

class DoobieTagRepository(doobie: DoobieContext) extends TagRepository with ConnectionIOFaker {

  import doobie.ctx._

  private val tags = quote(querySchema[Tag]("tags"))

  /**
   * create a Tag
   *
   * @param data Instance of ExternalResource
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: Tag): ConnectionIO[Long] = {
    val q = quote(
      tags
        .insert(lift(data))
        .onConflictUpdate((existingRow, newRow) => existingRow.name -> (newRow.name))
    )
    run(q)
  }

  /**
   * create a Tag
   *
   * @param data List of Tag
   * @return dummy long id (Doobie return Int)
   *
   * TODO: remove Option
   * TODO: return ConnectionIO[Long]
   */
  def bulkUpsert(data: Option[List[Tag]]): ConnectionIO[Int] = {
    data match {
      case None => ConnectionIOWithInt
      case Some(x) =>
        val q = s"""
          INSERT INTO tags (id, name)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            name = VALUES(name)
        """
        Update[Tag](q)
          .updateMany(x)
    }
  }

}
