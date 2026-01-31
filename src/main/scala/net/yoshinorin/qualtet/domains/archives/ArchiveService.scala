package net.yoshinorin.qualtet.domains.archives

import cats.Monad
import cats.effect.IO
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.{ContentTypeNotFound, DomainError}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

import scala.annotation.nowarn

class ArchiveService[F[_]: Monad @nowarn](
  archiveRepositoryAdapter: ArchiveRepositoryAdapter[F],
  contentTypeService: ContentTypeService[F]
)(using executer: Executer[F, IO], loggerFactory: Log4CatsLoggerFactory[IO]) {

  private given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  def get: IO[Either[DomainError, Seq[ArchiveResponseModel]]] = {
    ContentTypeName("article") match {
      case Left(error) => Left(error).logLeft[IO](Error)
      case Right(contentTypeName) =>
        for {
          maybeContentType <- contentTypeService.findByName(contentTypeName)
          result <- maybeContentType match {
            case Some(c) =>
              executer.transact(archiveRepositoryAdapter.get(c.id)).map(articles => Right(articles))
            case None =>
              Left(ContentTypeNotFound(detail = "content-type not found: article")).logLeft[IO](Warn)
          }
        } yield result
    }
  }

}
