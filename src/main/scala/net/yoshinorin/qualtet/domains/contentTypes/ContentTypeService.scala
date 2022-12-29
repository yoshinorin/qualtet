package net.yoshinorin.qualtet.domains.contentTypes

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.utils.Action._
import net.yoshinorin.qualtet.utils.{Action, Continue}
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext
import net.yoshinorin.qualtet.syntax._
import net.yoshinorin.qualtet.domains.Cacheable

class ContentTypeService(
  contentRepository: ContentTypeRepository[ConnectionIO],
  cache: CacheModule[String, ContentType]
)(dbContext: DataBaseContext[Aux[IO, Unit]])
    extends Cacheable {

  def upsertActions(data: ContentType): Action[Int] = {
    Continue(contentRepository.upsert(data), Action.buildDoneWithoutAnyHandle[Int])
  }

  def getAllActions: Action[Seq[ContentType]] = {
    Continue(contentRepository.getAll(), Action.buildDoneWithoutAnyHandle[Seq[ContentType]])
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
          _ <- upsertActions(data).perform.andTransact(dbContext)
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

    def actions(name: String): Action[Option[ContentType]] = {
      Continue(contentRepository.findByName(name), Action.buildDoneWithoutAnyHandle[Option[ContentType]])
    }

    def fromDB(name: String): IO[Option[ContentType]] = {
      for {
        x <- actions(name).perform.andTransact(dbContext)
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
    getAllActions.perform.andTransact(dbContext)
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
