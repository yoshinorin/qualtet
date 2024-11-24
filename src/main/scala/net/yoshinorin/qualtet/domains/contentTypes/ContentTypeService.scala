package net.yoshinorin.qualtet.domains.contentTypes

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.errors.InternalServerError
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class ContentTypeService[F[_]: Monad](
  contentRepository: ContentTypeRepository[F],
  cache: CacheModule[String, ContentType]
)(using executer: Executer[F, IO])
    extends Cacheable {

  def upsertCont(data: ContentType): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      val w = ContentTypeWriteModel(id = data.id, name = data.name)
      contentRepository.upsert(w)
    }
  }

  def getAllCont: ContT[F, Seq[ContentType], Seq[ContentType]] = {
    ContT.apply[F, Seq[ContentType], Seq[ContentType]] { next =>
      contentRepository.getAll().map { cr =>
        cr.map { c => ContentType(c.id, c.name) }
      }
    }
  }

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
          _ <- executer.transact(upsertCont(data))
          c <- this.findByName(data.name).throwIfNone(InternalServerError("contentType not found"))
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

    def cont(name: String): ContT[F, Option[ContentType], Option[ContentType]] = {
      ContT.apply[F, Option[ContentType], Option[ContentType]] { next =>
        contentRepository.findByName(name).map {
          case Some(c) => Some(ContentType(c.id, c.name))
          case None => None
        }
      }
    }

    def fromDB(name: String): IO[Option[ContentType]] = {
      for {
        x <- executer.transact(cont(name))
      } yield (x, cache.put(name, x))._1
    }

    val maybeContentType = cache.get(name)
    maybeContentType match {
      case Some(_: ContentType) => IO(maybeContentType)
      case _ => fromDB(name)
    }
  }

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  def getAll: IO[Seq[ContentType]] = {
    executer.transact(getAllCont)
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
