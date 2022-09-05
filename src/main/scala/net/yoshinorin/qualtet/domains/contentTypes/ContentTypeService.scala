package net.yoshinorin.qualtet.domains.contentTypes

import cats.effect.IO
import doobie.ConnectionIO
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.DoobieAction._
import net.yoshinorin.qualtet.domains.{DoobieAction, DoobieContinue}
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.syntax._
import net.yoshinorin.qualtet.domains.Cacheable

class ContentTypeService(
  contentRepository: ContentTypeRepository[ConnectionIO],
  cache: CacheModule[String, ContentType]
)(doobieContext: DoobieContext)
    extends Cacheable {

  def upsertActions(data: ContentType): DoobieAction[Int] = {
    DoobieContinue(contentRepository.upsert(data), DoobieAction.buildDoneWithoutAnyHandle[Int])
  }

  def getAllActions: DoobieAction[Seq[ContentType]] = {
    DoobieContinue(contentRepository.getAll(), DoobieAction.buildDoneWithoutAnyHandle[Seq[ContentType]])
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
          _ <- upsertActions(data).perform.andTransact(doobieContext)
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

    def actions(name: String): DoobieAction[Option[ContentType]] = {
      DoobieContinue(contentRepository.findByName(name), DoobieAction.buildDoneWithoutAnyHandle[Option[ContentType]])
    }

    def fromDB(name: String): IO[Option[ContentType]] = {
      for {
        x <- actions(name).perform.andTransact(doobieContext)
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
    getAllActions.perform.andTransact(doobieContext)
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
