package net.yoshinorin.qualtet.domains.archives

import cats.effect.IO
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.domains.ServiceLogic._
import net.yoshinorin.qualtet.domains.{ServiceLogic, Continue, Done}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

class ArchiveService(
  contentTypeService: ContentTypeService
)(
  implicit doobieContext: DoobieContextBase
) extends ServiceBase {

  def get: IO[Seq[ResponseArchive]] = {

    def execute(contentTypeId: ContentTypeId): ServiceLogic[Seq[ResponseArchive]] = {
      val request = GetByContentTypeId(contentTypeId)
      val resultHandler: Seq[ResponseArchive] => ServiceLogic[Seq[ResponseArchive]] = (resultHandler: Seq[ResponseArchive]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    for {
      c <- findBy("article", NotFound(s"content-type not found: article"))(contentTypeService.findByName)
      articles <- runWithTransaction(execute(c.id))(doobieContext)
    } yield articles
  }

}
