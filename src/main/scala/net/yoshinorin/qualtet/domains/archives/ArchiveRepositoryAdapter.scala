package net.yoshinorin.qualtet.domains.archives

import cats.Monad
import cats.data.ContT
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

class ArchiveRepositoryAdapter[F[_]: Monad](
  archiveRepository: ArchiveRepository[F]
) {

  private[domains] def get(contentTypeId: ContentTypeId): ContT[F, Seq[ArchiveResponseModel], Seq[ArchiveResponseModel]] = {
    ContT.apply[F, Seq[ArchiveResponseModel], Seq[ArchiveResponseModel]] { next =>
      archiveRepository.get(contentTypeId).map { archives =>
        archives.map(a => ArchiveResponseModel(a.path, a.title, a.publishedAt))
      }
    }
  }
}
