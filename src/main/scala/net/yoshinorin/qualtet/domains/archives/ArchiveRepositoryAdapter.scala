package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.period.Period

import cats.Monad
import cats.data.ContT
import cats.implicits.*

class ArchiveRepositoryAdapter[F[_]: Monad](
  archiveRepository: ArchiveRepository[F]
) {

  private[domains] def get(contentTypeId: ContentTypeId, periods: Seq[Period]): ContT[F, Seq[ArchiveResponseModel], Seq[ArchiveResponseModel]] = {
    ContT.apply[F, Seq[ArchiveResponseModel], Seq[ArchiveResponseModel]] { _ =>
      archiveRepository.get(contentTypeId, periods).map { archives =>
        archives.map(a => ArchiveResponseModel(a.path, a.title, a.publishedAt))
      }
    }
  }
}
