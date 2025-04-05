package net.yoshinorin.qualtet.domains.tags

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingRepositoryAdapter
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.errors.TagNotFound
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.syntax.*

class TagService[F[_]: Monad](
  tagRepositoryAdapter: TagRepositoryAdapter[F],
  cache: CacheModule[IO, String, Seq[TagResponseModel]],
  contentTaggingRepositoryAdapter: ContentTaggingRepositoryAdapter[F]
)(using executer: Executer[F, IO])
    extends Cacheable {

  private val CACHE_KEY = "TAGS_FULL_CACHE"

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[TagResponseModel]] = {

    def fromDB(): IO[Seq[TagResponseModel]] = {
      executer.transact(tagRepositoryAdapter.getAll)
    }

    for {
      maybeTags <- cache.get(CACHE_KEY)
      tags <- maybeTags match {
        case Some(t: Seq[TagResponseModel]) => IO.pure(t)
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
  def findById(id: TagId): IO[Option[Tag]] = {
    executer.transact(tagRepositoryAdapter.findById(id))
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {
    executer.transact(tagRepositoryAdapter.findByName(tagName))
  }

  /**
   * find tag from db or create new instance (just create instance, no insert to DB)
   *
   * @param tagName
   * @return Tag instance
   */
  private def findOrCreateNewInstance(tagName: TagName): IO[Tag] = {
    this.findByName(tagName).flatMap {
      case None => IO(Tag(TagId.apply(), tagName))
      case Some(t) => IO(t)
    }
  }

  /**
   * get tag from db or new instance
   *
   * @param tagNames
   * @return
   */
  def getTags(tagNames: Option[List[String]]): IO[Option[List[Tag]]] = {
    tagNames match {
      case None => IO(None)
      case Some(t) => t.map { t => findOrCreateNewInstance(TagName(t)) }.sequence.option
    }
  }

  /**
   * delete a tag and related data by TagId
   *
   * @param id Instance of TagId
   */
  def delete(id: TagId): IO[Unit] = {
    val queries = for {
      contentTaggingDelete <- executer.perform(contentTaggingRepositoryAdapter.deleteByTagId(id))
      tagDelete <- executer.perform(tagRepositoryAdapter.delete(id))
    } yield (contentTaggingDelete, tagDelete)

    for {
      _ <- this.findById(id).throwIfNone(TagNotFound(detail = s"tag not found: ${id}"))
      _ <- executer.transact2(queries)
    } yield ()
  }

  def invalidate(): IO[Unit] = {
    cache.invalidate()
  }
}
