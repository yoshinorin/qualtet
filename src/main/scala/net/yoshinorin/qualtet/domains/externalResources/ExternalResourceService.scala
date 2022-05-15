package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.externalResources.RepositoryRequests._

class ExternalResourceService() {

  /**
   * create are ExternalResources bulky without transaction
   *
   * @param Robots instance
   * @return dummy long id (Doobie return Int)
   *
   */
  def bulkUpsertWithoutTaransact(data: Option[List[ExternalResource]]): ConnectionIO[Int] = {

    def makeRequest(data: Option[List[ExternalResource]]): (BulkUpsert, ConnectionIO[Int] => ConnectionIO[Int]) = {
      val request = BulkUpsert(data)
      val resultHandler: ConnectionIO[Int] => ConnectionIO[Int] = (connectionIO: ConnectionIO[Int]) => { connectionIO }
      (request, resultHandler)
    }

    def run(data: Option[List[ExternalResource]]): ConnectionIO[Int] = {
      val (request, resultHandler) = makeRequest(data)
      ExternalResourceRepository.dispatch(request)
    }

    run(data)
  }
}
