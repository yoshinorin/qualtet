package net.yoshinorin.qualtet.domains.archives

import cats.effect.IO
import doobie.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.ServiceBase
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

    def makeRequest(contentTypeId: ContentTypeId): (GetByContentTypeId, ConnectionIO[Seq[ResponseArchive]] => ConnectionIO[Seq[ResponseArchive]]) = {
      val request = GetByContentTypeId(contentTypeId)
      val resultHandler: ConnectionIO[Seq[ResponseArchive]] => ConnectionIO[Seq[ResponseArchive]] =
        (connectionIO: ConnectionIO[Seq[ResponseArchive]]) => { connectionIO }
      (request, resultHandler)
    }

    def run(contentTypeId: ContentTypeId): IO[Seq[ResponseArchive]] = {
      val (request, _) = makeRequest(contentTypeId)
      ArchiveRepository.dispatch(request).transact(doobieContext.transactor)
    }

    for {
      c <- findBy("article", NotFound(s"content-type not found: article"))(contentTypeService.findByName)
      articles <- run(c.id)
    } yield articles
  }

}
