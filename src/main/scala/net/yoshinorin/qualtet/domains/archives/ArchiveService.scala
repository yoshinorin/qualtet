package net.yoshinorin.qualtet.domains.archives

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.syntax._

class ArchiveService(
  archiveRepository: ArchiveRepository[ConnectionIO],
  contentTypeService: ContentTypeService
)(dbContext: DataBaseContext[Aux[IO, Unit]]) {

  def actions(contentTypeId: ContentTypeId): Action[Seq[ResponseArchive]] = {
    Continue(archiveRepository.get(contentTypeId), Action.buildDoneWithoutAnyHandle[Seq[ResponseArchive]])
  }

  def get: IO[Seq[ResponseArchive]] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(NotFound(s"content-type not found: article"))
      articles <- actions(c.id).perform.andTransact(dbContext)
    } yield articles
  }

}
