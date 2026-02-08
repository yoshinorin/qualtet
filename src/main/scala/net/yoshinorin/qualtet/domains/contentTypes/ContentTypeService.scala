package net.yoshinorin.qualtet.domains.contentTypes

import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.errors.{DomainError, UnexpectedException}
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

import scala.annotation.nowarn

class ContentTypeService[G[_]: Monad, F[_]: Monad](
  contentTypeRepositoryAdapter: ContentTypeRepositoryAdapter[G],
  cache: CacheModule[F, String, ContentType]
)(using executer: Executer[G, F], loggerFactory: Log4CatsLoggerFactory[F])
    extends Cacheable[F] {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  /**
   * create a contentType
   *
   * @param name String
   * @return
   */
  def create(data: ContentType): F[Either[DomainError, ContentType]] = {
    this.findByName(data.name).flatMap {
      case Some(x: ContentType) => Monad[F].pure(Right(x))
      case None =>
        for {
          _ <- executer.transact(contentTypeRepositoryAdapter.upsert(data))
          maybeContentType <- this.findByName(data.name)
          result <- maybeContentType match {
            case Some(c) => Monad[F].pure(Right(c))
            case None => Left(UnexpectedException("contentType not found")).logLeft[F](Error)
          }
        } yield result
    }
  }

  /**
   * find a ContentType by name
   *
   * @param name name of ContentType
   * @return ContentType
   */
  def findByName(name: ContentTypeName): F[Option[ContentType]] = {

    def fromDB(name: ContentTypeName): F[Option[ContentType]] = {
      executer.transact(contentTypeRepositoryAdapter.findByName(name))
    }

    for {
      maybeContentType <- cache.get(name.value)
      contentType <- maybeContentType match {
        case Some(c: ContentType) => Monad[F].pure(Some(c))
        case _ =>
          for {
            maybeDbContentType <- fromDB(name)
            _ <- maybeDbContentType match {
              case Some(dc: ContentType) => cache.put(name.value, dc)
              case _ => Monad[F].pure(())
            }
          } yield maybeDbContentType
      }
    } yield contentType
  }

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  def getAll: F[Seq[ContentType]] = {
    executer.transact(contentTypeRepositoryAdapter.getAll)
  }

  def invalidate(): F[Unit] = {
    cache.invalidate()
  }

}
