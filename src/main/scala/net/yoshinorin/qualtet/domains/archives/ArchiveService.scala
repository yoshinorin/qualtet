package net.yoshinorin.qualtet.domains.archives

import cats.effect.IO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue, Done}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.syntax._

class ArchiveService(contentTypeService: ContentTypeService)(doobieContext: DoobieContextBase) {

  def get: IO[Seq[ResponseArchive]] = {

    def actions(contentTypeId: ContentTypeId): Action[Seq[ResponseArchive]] = {
      val request = GetByContentTypeId(contentTypeId)
      val resultHandler: Seq[ResponseArchive] => Action[Seq[ResponseArchive]] = (resultHandler: Seq[ResponseArchive]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    for {
      c <- contentTypeService.findByName("article").throwIfNone(NotFound(s"content-type not found: article"))
      articles <- actions(c.id).perform.andTransact(doobieContext)
    } yield articles
  }

}
