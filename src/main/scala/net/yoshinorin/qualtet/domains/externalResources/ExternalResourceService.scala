package net.yoshinorin.qualtet.domains.externalResources

import cats.data.ContT
import cats.implicits.*
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

  def findByContentIdCont(contenId: ContentId): ContT[F, Seq[ExternalResource], Seq[ExternalResource]] = {
    ContT.apply[F, Seq[ExternalResource], Seq[ExternalResource]] { next =>
      externalResourceRepository.findByContentId(contenId).map { x =>
        x.map { e =>
          ExternalResource(e.contentId, e.kind, e.name)
        }
      }
    }
  }

  def deleteCont(contentId: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      externalResourceRepository.delete(contentId)
    }
  }

}
