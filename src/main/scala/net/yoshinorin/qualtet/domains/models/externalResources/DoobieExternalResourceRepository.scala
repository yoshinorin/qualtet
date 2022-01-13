package net.yoshinorin.qualtet.domains.models.externalResources

import doobie.ConnectionIO
import doobie.util.update.Update
import net.yoshinorin.qualtet.infrastructure.db.doobie.{ConnectionIOFaker, DoobieContextBase}

class DoobieExternalResourceRepository(doobie: DoobieContextBase) extends ExternalResourceRepository with ConnectionIOFaker {

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
      case None => ConnectionIOWithInt
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
