package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.ServiceLogic._
import net.yoshinorin.qualtet.domains.{ServiceLogic, Continue, Done}

class ExternalResourceService() {

  /**
   * create are ExternalResources bulky without transaction
   *
   * @param Robots instance
   * @return dummy long id (Doobie return Int)
   *
   */
  def bulkUpsertWithoutTaransact(data: Option[List[ExternalResource]]): ConnectionIO[Int] = {

    def perform(data: Option[List[ExternalResource]]): ServiceLogic[Int] = {
      val request = BulkUpsert(data)
      val resultHandler: Int => ServiceLogic[Int] = (resultHandler: Int) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    perform(data).connect()
  }
}
