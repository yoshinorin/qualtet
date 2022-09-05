package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.{DoobieAction, DoobieContinue}
import net.yoshinorin.qualtet.domains.contents.ContentId

class RobotsService(
  robotsRepository: RobotsRepository[ConnectionIO]
) {
  def upsertActions(data: Robots): DoobieAction[Int] = {
    DoobieContinue(robotsRepository.upsert(data), DoobieAction.buildDoneWithoutAnyHandle[Int])
  }

  def deleteActions(contentId: ContentId): DoobieAction[Unit] = {
    DoobieContinue(robotsRepository.delete(contentId), DoobieAction.buildDoneWithoutAnyHandle[Unit])
  }
}
