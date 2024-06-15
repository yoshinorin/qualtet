package net.yoshinorin.qualtet.domains.archives

import cats.effect.IO
import net.yoshinorin.qualtet.actions.Action.*
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.syntax.*
import cats.Monad

class ArchiveService[F[_]: Monad](
  archiveRepository: ArchiveRepository[F],
  contentTypeService: ContentTypeService[F]
)(using executer: Executer[F, IO]) {

  def actions(contentTypeId: ContentTypeId): Action[Seq[ResponseArchive]] = {
    Continue(archiveRepository.get(contentTypeId), Action.done[Seq[ResponseArchive]])
  }

  def get: IO[Seq[ResponseArchive]] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(NotFound(detail = "content-type not found: article"))
      articles <- executer.transact(actions(c.id))
    } yield articles
  }

}
