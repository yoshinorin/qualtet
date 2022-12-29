package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.utils.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.ContentId

class ExternalResourceService(
  externalResourceRepository: ExternalResourceRepository[ConnectionIO]
) {

  def bulkUpsertActions(data: List[ExternalResource]): Action[Int] = {
    Continue(externalResourceRepository.bulkUpsert(data), Action.buildDoneWithoutAnyHandle[Int])
  }

  def deleteActions(contentId: ContentId): Action[Unit] = {
    Continue(externalResourceRepository.delete(contentId), Action.buildDoneWithoutAnyHandle[Unit])
  }

}
