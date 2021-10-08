package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.InternalServerError
import net.yoshinorin.qualtet.domains.models.contents.ContentId
import net.yoshinorin.qualtet.domains.models.robots.{Robots, RobotsRepository}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class RobotsService(
  robotsRepository: RobotsRepository
)(implicit doobieContext: DoobieContext) {

  def create(data: Robots): IO[Robots] = {

    def robots: IO[Robots] = this.findByContentId(data.contentId).flatMap {
      case None => IO.raiseError(InternalServerError("content not found")) //NOTE: 404 is better?
      case Some(x) => IO(x)
    }

    for {
      _ <- robotsRepository.upsert(data).transact(doobieContext.transactor)
      r <- robots
    } yield r
  }

  /**
   * find a robots by ContentId
   *
   * @param data Instance of ContentId
   * @return Robots instance
   */
  def findByContentId(contentId: ContentId): IO[Option[Robots]] = {
    robotsRepository.findByContentId(contentId).transact(doobieContext.transactor)
  }

}
