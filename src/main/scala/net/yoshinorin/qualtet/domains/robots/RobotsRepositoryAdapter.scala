package net.yoshinorin.qualtet.domains.robots

import cats.data.ContT
import cats.Monad
import net.yoshinorin.qualtet.domains.contents.ContentId

import scala.annotation.nowarn

class RobotsRepositoryAdapter[F[_]: Monad @nowarn](
  robotsRepository: RobotsRepository[F]
) {
  private[domains] def upsert(data: Robots): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { _ =>
      val w = RobotsWriteModel(contentId = data.contentId, attributes = data.attributes)
      robotsRepository.upsert(w)
    }
  }

  private[domains] def delete(contentId: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      robotsRepository.delete(contentId)
    }
  }
}
