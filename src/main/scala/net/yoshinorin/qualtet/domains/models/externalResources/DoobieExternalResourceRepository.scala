package net.yoshinorin.qualtet.domains.models.externalResources

import cats.implicits.catsSyntaxApplicativeId
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.update.Update
import io.getquill.{idiom => _}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieExternalResourceRepository(doobie: DoobieContext) extends ExternalResourceRepository {

  import doobie.ctx._

  private val externalResources = quote(querySchema[ExternalResource]("external_resources"))

  /**
   * create a externalResource (for meta)
   *
   * @param data Instance of ExternalResource
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: ExternalResource): ConnectionIO[Long] = {
    println(data)
    val q = quote(
      externalResources
        .insert(lift(data))
        .onConflictUpdate(
          (existingRow, newRow) => existingRow.kind -> (newRow.kind),
          (existingRow, newRow) => existingRow.name -> (newRow.name)
        )
    )
    run(q)
  }

  /**
   * create a externalResources (for meta)
   *
   * @param data List of ExternalResources
   * @return dummy long id (Doobie return Int)
   *
   *
   * TODO: remove Option
   * TODO: return ConnectionIO[Long]
   */
  def bulkUpsert(data: Option[List[ExternalResource]]): ConnectionIO[Int] = {
    data match {
      case None => 0.pure[ConnectionIO]
      case Some(x) =>
        val q = s"""
          INSERT INTO external_resources (content_id, kind, name)
            VALUES (?, ?, ?)
          ON DUPLICATE KEY UPDATE
            content_id = VALUES(content_id),
            kind = VALUES(kind),
            name = VALUES(name)
        """
        Update[ExternalResource](q)
          .updateMany(x)
    }
  }
}
