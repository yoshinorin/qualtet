package net.yoshinorin.qualtet.domains.externalResources

import cats.Monad
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.ContentId

class ExternalResourceService[F[_]: Monad](
  externalResourceRepository: ExternalResourceRepository[F]
) {

  def bulkUpsertActions(data: List[ExternalResource]): Action[Int] = {
    Continue(externalResourceRepository.bulkUpsert(data), Action.done[Int])
  }

  def deleteActions(contentId: ContentId): Action[Unit] = {
    Continue(externalResourceRepository.delete(contentId), Action.done[Unit])
  }

}
