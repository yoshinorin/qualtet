package net.yoshinorin.qualtet.domains.externalResources

import cats.data.ContT
import cats.Monad
import net.yoshinorin.qualtet.domains.contents.ContentId

class ExternalResourceService[F[_]: Monad](
  externalResourceRepository: ExternalResourceRepository[F]
) {

  def bulkUpsertCont(data: List[ExternalResource]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      val ws = data.map { d =>
        ExternalResourceWriteModel(contentId = d.contentId, kind = d.kind, name = d.name)
      }
      externalResourceRepository.bulkUpsert(ws)
    }
  }

  def deleteCont(contentId: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      externalResourceRepository.delete(contentId)
    }
  }

}
