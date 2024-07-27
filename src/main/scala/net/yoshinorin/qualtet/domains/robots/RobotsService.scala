package net.yoshinorin.qualtet.domains.robots

import cats.data.ContT
import cats.Monad
import net.yoshinorin.qualtet.domains.contents.ContentId

class RobotsService[F[_]: Monad](
  robotsRepository: RobotsRepository[F]
) {
  def upsertCont(data: Robots): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      robotsRepository.upsert(data)
    }
  }

  def deleteCont(contentId: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      robotsRepository.delete(contentId)
    }
  }
}
