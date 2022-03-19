package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.models.Fail.InternalServerError
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, ContentTypeRepository}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class ContentTypeService(
  contentTypeRepository: ContentTypeRepository,
  cache: CacheModule[String, ContentType]
)(
  implicit doobieContext: DoobieContextBase
) extends ServiceBase {

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
          _ <- contentTypeRepository.upsert(data).transact(doobieContext.transactor)
          c <- findBy(data.name, InternalServerError("contentType not found"))(this.findByName)
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
      for {
        x <- contentTypeRepository.findByName(name).transact(doobieContext.transactor)
      } yield (x, cache.put(name, x))._1
    }

    val maybeContentType = cache.get(name)
    maybeContentType match {
      case Some(x: ContentType) => IO(maybeContentType)
      case _ => fromDB(name)
    }
  }

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  def getAll: IO[Seq[ContentType]] = {
    contentTypeRepository.getAll.transact(doobieContext.transactor)
  }

}
