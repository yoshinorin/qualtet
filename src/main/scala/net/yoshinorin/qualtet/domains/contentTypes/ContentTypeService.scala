package net.yoshinorin.qualtet.domains.contentTypes

import cats.effect.IO
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.domains.ServiceLogic._
import net.yoshinorin.qualtet.domains.{ServiceLogic, Continue, Done}
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

    def perform(data: ContentType): ServiceLogic[Int] = {
      val request = Upsert(data)
      val resultHandler: Int => ServiceLogic[Int] = (resultHandler: Int) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    this.findByName(data.name).flatMap {
      case Some(x: ContentType) => IO(x)
      case None =>
        for {
          _ <- transact(perform(data))(doobieContext)
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

    def perform(name: String): ServiceLogic[Option[ContentType]] = {
      val request = FindByName(name)
      val resultHandler: Option[ContentType] => ServiceLogic[Option[ContentType]] = (resultHandler: Option[ContentType]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    def fromDB(name: String): IO[Option[ContentType]] = {
      for {
        x <- transact(perform(name))(doobieContext)
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

    def perform(): ServiceLogic[Seq[ContentType]] = {
      val request = GetAll()
      val resultHandler: Seq[ContentType] => ServiceLogic[Seq[ContentType]] = (resultHandler: Seq[ContentType]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    transact(perform())(doobieContext)
  }

}
