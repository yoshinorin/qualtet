package net.yoshinorin.qualtet.domains.contentTypes

import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class ContentTypeService(cache: CacheModule[String, ContentType])(implicit doobieContext: DoobieContextBase) extends ServiceBase {

  /**
   * create a contentType
   *
   * @param name String
   * @return
   */
  def create(data: ContentType): IO[ContentType] = {

    def makeRequest(data: ContentType): (Upsert, ConnectionIO[Int] => ConnectionIO[Int]) = {
      val request = Upsert(data)
      val resultHandler: ConnectionIO[Int] => ConnectionIO[Int] = (connectionIO: ConnectionIO[Int]) => { connectionIO }
      (request, resultHandler)
    }

    def run(data: ContentType): IO[Int] = {
      val (request, resultHandler) = makeRequest(data)
      ContentTypeRepository.dispatch(request).transact(doobieContext.transactor)
    }

    this.findByName(data.name).flatMap {
      case Some(x: ContentType) => IO(x)
      case None =>
        for {
          _ <- run(data)
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

    def makeRequest(name: String): (FindByName, ConnectionIO[Option[ContentType]] => ConnectionIO[Option[ContentType]]) = {
      val request = FindByName(name)
      val resultHandler: ConnectionIO[Option[ContentType]] => ConnectionIO[Option[ContentType]] = (connectionIO: ConnectionIO[Option[ContentType]]) => {
        connectionIO
      }
      (request, resultHandler)
    }

    def run(name: String): IO[Option[ContentType]] = {
      val (request, resultHandler) = makeRequest(name)
      ContentTypeRepository.dispatch(request).transact(doobieContext.transactor)
    }

    def fromDB(name: String): IO[Option[ContentType]] = {
      for {
        x <- run(name)
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

    def makeRequest(): (GetAll, ConnectionIO[Seq[ContentType]] => ConnectionIO[Seq[ContentType]]) = {
      val request = GetAll()
      val resultHandler: ConnectionIO[Seq[ContentType]] => ConnectionIO[Seq[ContentType]] = (connectionIO: ConnectionIO[Seq[ContentType]]) => { connectionIO }
      (request, resultHandler)
    }

    def run(): IO[Seq[ContentType]] = {
      val (request, resultHandler) = makeRequest()
      ContentTypeRepository.dispatch(request).transact(doobieContext.transactor)
    }

    run()
  }

}
