package net.yoshinorin.qualtet.domains.contentTypes

import cats.effect.IO
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue, Done}
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class ContentTypeService(cache: CacheModule[String, ContentType])(doobieContext: DoobieContextBase) extends ServiceBase {

  /**
   * create a contentType
   *
   * @param name String
   * @return
   */
  def create(data: ContentType): IO[ContentType] = {

    def actions(data: ContentType): Action[Int] = {
      val request = Upsert(data)
      val resultHandler: Int => Action[Int] = (resultHandler: Int) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    this.findByName(data.name).flatMap {
      case Some(x: ContentType) => IO(x)
      case None =>
        for {
          _ <- actions(data).perform.andTransact(doobieContext)
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

    def actions(name: String): Action[Option[ContentType]] = {
      val request = FindByName(name)
      val resultHandler: Option[ContentType] => Action[Option[ContentType]] = (resultHandler: Option[ContentType]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
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
      val request = GetAll()
      val resultHandler: Seq[ContentType] => Action[Seq[ContentType]] = (resultHandler: Seq[ContentType]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    actions.perform.andTransact(doobieContext)
  }

}
