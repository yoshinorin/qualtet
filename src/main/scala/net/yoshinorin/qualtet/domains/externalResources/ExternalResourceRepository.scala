package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker
import net.yoshinorin.qualtet.domains.externalResources.RepositoryRequests._

object ExternalResourceRepository extends ConnectionIOFaker {

  /**
   * create a externalResources (for meta)
   *
   * @param request BulkUpsert case class
   * @return dummy long id (Doobie return Int)
   *
   * TODO: return ConnectionIO[Long]
   */
  def dispatch(request: BulkUpsert): ConnectionIO[Int] = {
    request.data match {
      case None => ConnectionIOWithInt
      case Some(x) =>
        ExternalResourceQuery.bulkUpsert.updateMany(x)
    }
  }
}
