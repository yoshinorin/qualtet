package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.{DoobieAction, DoobieContinue}
import net.yoshinorin.qualtet.domains.contents.ContentId

class ExternalResourceService(
  externalResourceRepository: ExternalResourceRepository[ConnectionIO]
) {

  def bulkUpsertActions(data: List[ExternalResource]): DoobieAction[Int] = {
    DoobieContinue(externalResourceRepository.bulkUpsert(data), DoobieAction.buildDoneWithoutAnyHandle[Int])
  }

  def deleteActions(contentId: ContentId): DoobieAction[Unit] = {
    DoobieContinue(externalResourceRepository.delete(contentId), DoobieAction.buildDoneWithoutAnyHandle[Unit])
  }

}
