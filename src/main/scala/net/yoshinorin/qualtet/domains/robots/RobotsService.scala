package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.ContentId

class RobotsService(
  robotsRepository: RobotsRepository[ConnectionIO]
) {
  def upsertActions(data: Robots): Action[Int] = {
    Continue(robotsRepository.upsert(data), Action.done[Int])
  }

  def deleteActions(contentId: ContentId): Action[Unit] = {
    Continue(robotsRepository.delete(contentId), Action.done[Unit])
  }
}
