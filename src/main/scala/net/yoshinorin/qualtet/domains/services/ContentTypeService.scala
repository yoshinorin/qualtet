package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.InternalServerError
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, ContentTypeRepository}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.utils.Cache

class ContentTypeService(
  contentTypeRepository: ContentTypeRepository,
  cache: Cache[String, ContentType]
)(
  implicit doobieContext: DoobieContext
) {

  /**
   * create a contentType
   *
   * @param name String
   * @return
   */
  def create(data: ContentType): IO[ContentType] = {

    def contentType: IO[ContentType] = this.findByName(data.name).flatMap {
      case None => IO.raiseError(InternalServerError("contentType not found")) //NOTE: 404 is better?
      case Some(x: ContentType) => IO(x)
    }

    this.findByName(data.name).flatMap {
      case Some(x: ContentType) => IO(x)
      case None =>
        for {
          _ <- contentTypeRepository.create(data).transact(doobieContext.transactor)
          c <- contentType
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
