package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.externalResources.RepositoryRequests._

trait ExternalResourceRepository {

  /**
   * create a externalResources (for meta)
   *
   * @param request BulkUpsert case class
   * @return dummy long id (Doobie return Int)
   *
   * TODO: return ConnectionIO[Long]
   */
  def dispatch(request: BulkUpsert): ConnectionIO[Int]

}
