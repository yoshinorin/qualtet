package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue, Done}

class ExternalResourceService() {

  /**
   * create are ExternalResources bulky without transaction
   *
   * @param Robots instance
   * @return dummy long id (Doobie return Int)
   *
   */
  def bulkUpsertWithoutTaransact(data: Option[List[ExternalResource]]): ConnectionIO[Int] = {

    def actions(data: Option[List[ExternalResource]]): Action[Int] = {
      val request = BulkUpsert(data)
      val resultHandler: Int => Action[Int] = (resultHandler: Int) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    actions(data).connect()
  }
}
