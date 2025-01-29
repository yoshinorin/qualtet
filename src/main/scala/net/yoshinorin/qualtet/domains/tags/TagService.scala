package net.yoshinorin.qualtet.domains.tags

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingService
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.errors.TagNotFound
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.syntax.*

class TagService[F[_]: Monad](
  tagRepository: TagRepository[F],
  cache: CacheModule[String, Seq[TagResponseModel]],
  contentTaggingService: ContentTaggingService[F]
)(using executer: Executer[F, IO])
    extends Cacheable {

  private val cacheKey = "tags-full-cache"

  def bulkUpsertCont(data: Option[List[Tag]]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      data match {
        case Some(d) => {
          val ws = d.map { t => TagWriteModel(id = t.id, name = t.name) }
          tagRepository.bulkUpsert(ws)
        }
        case None => tagRepository.fakeRequest()
      }
    }
  }

  def getAllCont: ContT[F, Seq[TagResponseModel], Seq[TagResponseModel]] = {
    ContT.apply[F, Seq[TagResponseModel], Seq[TagResponseModel]] { next =>
      tagRepository.getAll().map { x =>
        x.map { case (cnt, tag) => TagResponseModel(count = cnt, id = tag.id, name = tag.name) }
      }
    }
  }

  def findByIdCont(id: TagId): ContT[F, Option[Tag], Option[Tag]] = {
    ContT.apply[F, Option[Tag], Option[Tag]] { next =>
      tagRepository.findById(id).map { x =>
        x.map { t =>
          Tag(t.id, t.name)
        }
      }
    }
  }

  def findByNameCont(tagName: TagName): ContT[F, Option[Tag], Option[Tag]] = {
    ContT.apply[F, Option[Tag], Option[Tag]] { next =>
      tagRepository.findByName(tagName).map { x =>
        x.map { t =>
          Tag(t.id, t.name)
        }
      }
    }
  }

  def findByContentIdCont(contenId: ContentId): ContT[F, Seq[Tag], Seq[Tag]] = {
    ContT.apply[F, Seq[Tag], Seq[Tag]] { next =>
      tagRepository.findByContentId(contenId).map { x =>
        x.map { t =>
          Tag(t.id, t.name)
        }
      }
    }
  }

  def deleteCont(id: TagId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      tagRepository.delete(id)
    }
  }

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[TagResponseModel]] = {

    def fromDB(): IO[Seq[TagResponseModel]] = {
      for {
        x <- executer.transact(getAllCont)
      } yield {
        cache.put(cacheKey, x)
        x
      }
    }

    cache.get(cacheKey) match {
      case Some(tags: Seq[TagResponseModel]) => IO(tags)
      case _ => fromDB()
    }
  }

  /**
   * find tag by id
   *
   * @param id
   * @return maybe Tag
   */
  def findById(id: TagId): IO[Option[Tag]] = {
    executer.transact(findByIdCont(id))
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {
    executer.transact(findByNameCont(tagName))
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
      contentTaggingDelete <- executer.perform(contentTaggingService.deleteByTagIdCont(id))
      tagDelete <- executer.perform(deleteCont(id))
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
