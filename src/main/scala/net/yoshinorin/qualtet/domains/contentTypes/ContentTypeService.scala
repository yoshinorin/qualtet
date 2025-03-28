package net.yoshinorin.qualtet.domains.contentTypes

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.errors.UnexpectedException
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class ContentTypeService[F[_]: Monad](
  contentTypeRepositoryAdapter: ContentTypeRepositoryAdapter[F],
  cache: CacheModule[String, ContentType]
)(using executer: Executer[F, IO])
    extends Cacheable {

  /**
   * create a contentType
   *
   * @param name String
   * @return
   */
  def create(data: ContentType): IO[ContentType] = {
    this.findByName(data.name).flatMap {
      case Some(x: ContentType) => IO(x)
      case None =>
        for {
          _ <- executer.transact(contentTypeRepositoryAdapter.upsert(data))
          c <- this.findByName(data.name).throwIfNone(UnexpectedException("contentType not found"))
        } yield c
    }

  }

  /**
   * find a ContentType by name
   *
   * @param name name of ContentType
   * @return ContentType
   */
  def findByName(name: String): IO[Option[ContentType]] = {

    def fromDB(name: String): IO[Option[ContentType]] = {
      executer.transact(contentTypeRepositoryAdapter.findByName(name))
    }

    val maybeContentType = cache.get(name)
    maybeContentType match {
      case Some(_: ContentType) => IO(maybeContentType)
      case _ =>
        for {
          contentType <- fromDB(name)
        } yield {
          cache.put(name, contentType)
          contentType
        }
    }
  }

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  def getAll: IO[Seq[ContentType]] = {
    executer.transact(contentTypeRepositoryAdapter.getAll)
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
