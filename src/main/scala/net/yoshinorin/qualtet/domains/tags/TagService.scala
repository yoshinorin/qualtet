package net.yoshinorin.qualtet.domains.tags

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingService
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.syntax.*

class TagService[F[_]: Monad](
  tagRepository: TagRepository[F],
  contentTaggingService: ContentTaggingService[F]
)(using executer: Executer[F, IO]) {

  def bulkUpsertActions(data: Option[List[Tag]]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      data match {
        case Some(d) => tagRepository.bulkUpsert(d)
        case None => tagRepository.fakeRequest()
      }
    }
  }

  def getAllActions: ContT[F, Seq[ResponseTag], Seq[ResponseTag]] = {
    ContT.apply[F, Seq[ResponseTag], Seq[ResponseTag]] { next =>
      tagRepository.getAll()
    }
  }

  def findByIdActions(id: TagId): ContT[F, Option[Tag], Option[Tag]] = {
    ContT.apply[F, Option[Tag], Option[Tag]] { next =>
      tagRepository.findById(id)
    }
  }

  def findByNameActions(tagName: TagName): ContT[F, Option[Tag], Option[Tag]] = {
    ContT.apply[F, Option[Tag], Option[Tag]] { next =>
      tagRepository.findByName(tagName)
    }
  }

  def findByContentIdActions(contenId: ContentId): ContT[F, Seq[Tag], Seq[Tag]] = {
    ContT.apply[F, Seq[Tag], Seq[Tag]] { next =>
      tagRepository.findByContentId(contenId)
    }
  }

  def deleteActions(id: TagId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      tagRepository.delete(id)
    }
  }

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[ResponseTag]] = {
    executer.transact(getAllActions)
  }

  /**
   * find tag by id
   *
   * @param id
   * @return maybe Tag
   */
  def findById(id: TagId): IO[Option[Tag]] = {
    executer.transact(findByIdActions(id))
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {
    executer.transact(findByNameActions(tagName))
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
      contentTaggingDelete <- executer.perform(contentTaggingService.deleteByTagIdActions(id))
      tagDelete <- executer.perform(deleteActions(id))
    } yield (contentTaggingDelete, tagDelete)

    for {
      _ <- this.findById(id).throwIfNone(NotFound(detail = s"tag not found: ${id}"))
      _ <- executer.transact2(queries)
    } yield ()
  }
}
