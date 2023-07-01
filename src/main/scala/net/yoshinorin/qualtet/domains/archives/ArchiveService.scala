package net.yoshinorin.qualtet.domains.archives

import cats.effect.IO
import net.yoshinorin.qualtet.actions.Action.*
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.infrastructure.db.Transactor
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.syntax.*
import cats.Monad

class ArchiveService[M[_]: Monad](
  archiveRepository: ArchiveRepository[M],
  contentTypeService: ContentTypeService[M]
)(using transactor: Transactor[M]) {

  def actions(contentTypeId: ContentTypeId): Action[Seq[ResponseArchive]] = {
    Continue(archiveRepository.get(contentTypeId), Action.done[Seq[ResponseArchive]])
  }

  def get: IO[Seq[ResponseArchive]] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(NotFound(s"content-type not found: article"))
      articles <- transactor.transact(actions(c.id))
    } yield articles
  }

}
