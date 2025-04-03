package net.yoshinorin.qualtet.domains.externalResources

import cats.data.ContT
import cats.implicits.*
import cats.Monad
import net.yoshinorin.qualtet.domains.contents.ContentId

class ExternalResourceRepositoryAdapter[F[_]: Monad](
  externalResourceRepository: ExternalResourceRepository[F]
) {

  private[domains] def bulkUpsert(data: List[ExternalResource]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      val ws = data.map { d =>
        ExternalResourceWriteModel(contentId = d.contentId, kind = d.kind, name = d.name)
      }
      externalResourceRepository.bulkUpsert(ws)
    }
  }

  private[domains] def findByContentId(contenId: ContentId): ContT[F, Seq[ExternalResource], Seq[ExternalResource]] = {
    ContT.apply[F, Seq[ExternalResource], Seq[ExternalResource]] { next =>
      externalResourceRepository.findByContentId(contenId).map { x =>
        x.map { e =>
          ExternalResource(e.contentId, e.kind, e.name)
        }
      }
    }
  }

  private[domains] def delete(contentId: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      externalResourceRepository.delete(contentId)
    }
  }

  private[domains] def bulkDelete(data: List[ExternalResourceDeleteModel]): ContT[F, Unit, Unit] = {
    data.size match {
      case 0 => ContT.apply[F, Unit, Unit] { _ => Monad[F].pure(()) }
      case _ => ContT.apply[F, Unit, Unit] { next => externalResourceRepository.bulkDelete(data) }
    }
  }
}
