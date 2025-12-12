package net.yoshinorin.qualtet.domains.archives

import cats.Monad
import cats.effect.IO
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.{ContentTypeNotFound, DomainError}
import net.yoshinorin.qualtet.infrastructure.db.Executer

class ArchiveService[F[_]: Monad](
  archiveRepositoryAdapter: ArchiveRepositoryAdapter[F],
  contentTypeService: ContentTypeService[F]
)(using executer: Executer[F, IO]) {

  def get: IO[Either[DomainError, Seq[ArchiveResponseModel]]] = {
    ContentTypeName("article") match {
      case Left(error) => IO.pure(Left(error))
      case Right(contentTypeName) =>
        for {
          maybeContentType <- contentTypeService.findByName(contentTypeName)
          result <- maybeContentType match {
            case Some(c) =>
              executer.transact(archiveRepositoryAdapter.get(c.id)).map(articles => Right(articles))
            case None =>
              IO.pure(Left(ContentTypeNotFound(detail = "content-type not found: article")))
          }
        } yield result
    }
  }

}
