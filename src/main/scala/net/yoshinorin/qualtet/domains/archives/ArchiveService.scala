package net.yoshinorin.qualtet.domains.archives

import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.{ContentTypeNotFound, DomainError}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

import scala.annotation.nowarn

class ArchiveService[F[_]: Monad, G[_]: Monad](
  archiveRepositoryAdapter: ArchiveRepositoryAdapter[G],
  contentTypeService: ContentTypeService[F, G]
)(using executer: Executer[F, G], loggerFactory: Log4CatsLoggerFactory[F]) {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  def get: F[Either[DomainError, Seq[ArchiveResponseModel]]] = {
    ContentTypeName("article") match {
      case Left(error) => Left(error).logLeft[F](Error)
      case Right(contentTypeName) =>
        for {
          maybeContentType <- contentTypeService.findByName(contentTypeName)
          result <- maybeContentType match {
            case Some(c) =>
              executer.transact(archiveRepositoryAdapter.get(c.id)).map(articles => Right(articles))
            case None =>
              Left(ContentTypeNotFound(detail = "content-type not found: article")).logLeft[F](Warn)
          }
        } yield result
    }
  }

}
