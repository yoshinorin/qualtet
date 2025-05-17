package net.yoshinorin.qualtet.domains.archives

import cats.Monad
import cats.effect.IO
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.ContentTypeNotFound
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class ArchiveService[F[_]: Monad](
  archiveRepositoryAdapter: ArchiveRepositoryAdapter[F],
  contentTypeService: ContentTypeService[F]
)(using executer: Executer[F, IO]) {

  def get: IO[Seq[ArchiveResponseModel]] = {
    for {
      c <- contentTypeService.findByName(ContentTypeName("article")).throwIfNone(ContentTypeNotFound(detail = "content-type not found: article"))
      articles <- executer.transact(archiveRepositoryAdapter.get(c.id))
    } yield articles
  }

}
