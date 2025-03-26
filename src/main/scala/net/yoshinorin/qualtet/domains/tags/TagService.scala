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
  cache: CacheModule[String, Seq[TagResponseModel]],
  contentTaggingRepositoryAdapter: ContentTaggingRepositoryAdapter[F]
)(using executer: Executer[F, IO])
    extends Cacheable {

  private val cacheKey = "tags-full-cache"

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[TagResponseModel]] = {

    def fromDB(): IO[Seq[TagResponseModel]] = {
      executer.transact(tagRepositoryAdapter.getAll)
    }

    cache.get(cacheKey) match {
      case Some(tags: Seq[TagResponseModel]) => IO(tags)
      case _ =>
        for {
          tags <- fromDB()
        } yield {
          cache.put(cacheKey, tags)
          tags
        }
    }
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
  def findOrGetNewInstance(tagName: TagName): IO[Tag] = {
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
      case Some(t) => t.map { t => findOrGetNewInstance(TagName(t)) }.sequence.option
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
    IO(cache.invalidate())
  }
}
