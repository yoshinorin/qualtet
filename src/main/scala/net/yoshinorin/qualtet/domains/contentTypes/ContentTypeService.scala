package net.yoshinorin.qualtet.domains.contentTypes

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.actions.Action.*
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class ContentTypeService[F[_]: Monad](
  contentRepository: ContentTypeRepository[F],
  cache: CacheModule[String, ContentType]
)(using executer: Executer[F, IO])
    extends Cacheable {

  def upsertActions(data: ContentType): Action[Int] = {
    Continue(contentRepository.upsert(data), Action.done[Int])
  }

  def getAllActions: Action[Seq[ContentType]] = {
    Continue(contentRepository.getAll(), Action.done[Seq[ContentType]])
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
          _ <- executer.transact(upsertActions(data))
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
      Continue(contentRepository.findByName(name), Action.done[Option[ContentType]])
    }

    def fromDB(name: String): IO[Option[ContentType]] = {
      for {
        x <- executer.transact(actions(name))
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
    executer.transact(getAllActions)
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
