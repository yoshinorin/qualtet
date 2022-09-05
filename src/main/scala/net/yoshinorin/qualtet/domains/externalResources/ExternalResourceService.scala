package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.{DoobieAction, DoobieContinue}
import net.yoshinorin.qualtet.domains.contents.ContentId

class ExternalResourceService(
  externalResourceRepository: ExternalResourceRepository[ConnectionIO]
) {

  def bulkUpsertActions(data: Option[List[ExternalResource]]): DoobieAction[Int] = {
    data match {
      case Some(value) => DoobieContinue(externalResourceRepository.bulkUpsert(value), DoobieAction.buildDoneWithoutAnyHandle[Int])
      case None => DoobieContinue(externalResourceRepository.fakeRequest(), DoobieAction.buildDoneWithoutAnyHandle[Int])
    }
  }

  def deleteActions(contentId: ContentId): DoobieAction[Unit] = {
    DoobieContinue(externalResourceRepository.delete(contentId), DoobieAction.buildDoneWithoutAnyHandle[Unit])
  }

}
