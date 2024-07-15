package net.yoshinorin.qualtet.domains.archives

import cats.Monad
import cats.data.ContT
import cats.effect.IO
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.syntax.*

class ArchiveService[F[_]: Monad](
  archiveRepository: ArchiveRepository[F],
  contentTypeService: ContentTypeService[F]
)(using executer: Executer[F, IO]) {

  def actions(contentTypeId: ContentTypeId): ContT[F, Seq[ResponseArchive], Seq[ResponseArchive]] = {
    ContT.apply[F, Seq[ResponseArchive], Seq[ResponseArchive]] { next =>
      archiveRepository.get(contentTypeId)
    }
  }

  def get: IO[Seq[ResponseArchive]] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(NotFound(detail = "content-type not found: article"))
      articles <- executer.transact(actions(c.id))
    } yield articles
  }

}
