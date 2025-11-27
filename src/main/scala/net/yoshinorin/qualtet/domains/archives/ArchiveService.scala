package net.yoshinorin.qualtet.domains.archives

import cats.Monad
import cats.effect.IO
import cats.implicits.*
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
      contentTypeName <- ContentTypeName("article").liftTo[IO]
      c <- contentTypeService.findByName(contentTypeName).errorIfNone(ContentTypeNotFound(detail = "content-type not found: article")).flatMap(_.liftTo[IO])
      articles <- executer.transact(archiveRepositoryAdapter.get(c.id))
    } yield articles
  }

}
