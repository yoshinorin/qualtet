package net.yoshinorin.qualtet.domains.tags

import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingRepositoryAdapter
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.errors.{DomainError, TagNotFound}
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.syntax.*

class TagService[G[_]: Monad, F[_]: Monad](
  tagRepositoryAdapter: TagRepositoryAdapter[G],
  cache: CacheModule[F, String, Seq[TagResponseModel]],
  contentTaggingRepositoryAdapter: ContentTaggingRepositoryAdapter[G]
)(using executer: Executer[G, F], loggerFactory: Log4CatsLoggerFactory[F])
    extends Cacheable[F] {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)
  private val CACHE_KEY = "TAGS_FULL_CACHE"

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: F[Seq[TagResponseModel]] = {

    def fromDB(): F[Seq[TagResponseModel]] = {
      executer.transact(tagRepositoryAdapter.getAll)
    }

    for {
      maybeTags <- cache.get(CACHE_KEY)
      tags <- maybeTags match {
        case Some(t: Seq[TagResponseModel]) => Monad[F].pure(t)
        case _ =>
          for {
            dbTags <- fromDB()
            _ <- cache.put(CACHE_KEY, dbTags)
          } yield dbTags
      }
    } yield tags
  }

  /**
   * find tag by id
   *
   * @param id
   * @return maybe Tag
   */
  def findById(id: TagId): F[Option[Tag]] = {
    executer.transact(tagRepositoryAdapter.findById(id))
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): F[Option[Tag]] = {
    executer.transact(tagRepositoryAdapter.findByName(tagName))
  }

  private def findOrCreateNewInstance(tag: Tag): F[Tag] = {
    this.findByName(tag.name).flatMap {
      case None => Monad[F].pure(Tag(TagId.apply(), tag.name, tag.path))
      case Some(t) => Monad[F].pure(t)
    }
  }

  def getTags(tags: Option[List[Tag]]): F[Option[List[Tag]]] = {
    tags match {
      case None => Monad[F].pure(None)
      case Some(t) =>
        t.map { t =>
          findOrCreateNewInstance(t)
        }.sequence
          .map(Some(_))
    }
  }

  /**
   * delete a tag and related data by TagId
   *
   * @param id Instance of TagId
   */
  def delete(id: TagId): F[Either[DomainError, Unit]] = {
    val queries = for {
      contentTaggingDelete <- executer.defer(contentTaggingRepositoryAdapter.deleteByTagId(id))
      tagDelete <- executer.defer(tagRepositoryAdapter.delete(id))
    } yield (contentTaggingDelete, tagDelete)

    for {
      maybeTag <- this.findById(id)
      result <- maybeTag match {
        case Some(_) =>
          executer.transact2(queries).map(_ => Right(()))
        case None =>
          Left(TagNotFound(detail = s"tag not found: ${id}")).logLeft[F](Warn)
      }
    } yield result
  }

  def invalidate(): F[Unit] = {
    cache.invalidate()
  }
}
