package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.NotFound
import net.yoshinorin.qualtet.domains.models.archives.{ArchiveRepository, ResponseArchive}
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentType
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class ArchiveService(
  archiveRepository: ArchiveRepository,
  contentTypeService: ContentTypeService
)(
  implicit doobieContext: DoobieContext
) {

  // TODO: move somewhere
  def contentType: IO[ContentType] = contentTypeService.findByName("article").flatMap {
    case None => IO.raiseError(NotFound(s"content-type not found: article"))
    case Some(x) => IO(x)
  }

  def get: IO[Seq[ResponseArchive]] = {
    for {
      c <- this.contentType // TODO: get from cache
      articles <- archiveRepository.get(c.id).transact(doobieContext.transactor)
    } yield articles
  }

}
