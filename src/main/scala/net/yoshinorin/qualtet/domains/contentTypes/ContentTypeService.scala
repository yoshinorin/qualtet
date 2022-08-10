package net.yoshinorin.qualtet.domains.contentTypes

import cats.effect.IO
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.syntax._
import net.yoshinorin.qualtet.domains.Cacheable

class ContentTypeService(cache: CacheModule[String, ContentType])(doobieContext: DoobieContext) extends Cacheable {

  /**
   * create a contentType
   *
   * @param name String
   * @return
   */
  def create(data: ContentType): IO[ContentType] = {

    def actions(data: ContentType): Action[Int] = {
      Continue(Upsert(data), Action.buildNext[Int])
    }

    this.findByName(data.name).flatMap {
      case Some(x: ContentType) => IO(x)
      case None =>
        for {
          _ <- actions(data).perform.andTransact(doobieContext)
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
      Continue(FindByName(name), Action.buildNext[Option[ContentType]])
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

    def actions: Action[Seq[ContentType]] = {
      Continue(GetAll(), Action.buildNext[Seq[ContentType]])
    }

    actions.perform.andTransact(doobieContext)
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
