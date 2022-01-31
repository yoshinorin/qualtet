package net.yoshinorin.qualtet.domains.models.externalResources

import doobie.ConnectionIO
import doobie.util.update.Update
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker

class DoobieExternalResourceRepository extends ExternalResourceRepository with ConnectionIOFaker {

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
        DoobieExternalResourceQuery
          .bulkUpsert(x)
          .updateMany(x)
    }
  }
}
