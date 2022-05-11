package net.yoshinorin.qualtet.domains.archives

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class ArchiveService(
  archiveRepository: ArchiveRepository,
  contentTypeService: ContentTypeService
)(
  implicit doobieContext: DoobieContextBase
) extends ServiceBase {

  def get: IO[Seq[ResponseArchive]] = {
    for {
      c <- findBy("article", NotFound(s"content-type not found: article"))(contentTypeService.findByName)
      articles <- archiveRepository.get(c.id).transact(doobieContext.transactor)
    } yield articles
  }

}
