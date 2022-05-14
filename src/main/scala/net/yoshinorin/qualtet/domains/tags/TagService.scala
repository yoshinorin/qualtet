package net.yoshinorin.qualtet.domains.tags

import cats.effect.IO
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.tags.RepositoryRequests.{BulkUpsert, FindByName, GetAll}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class TagService()(implicit doobieContext: DoobieContextBase) {

  /**
   * get all tags
   *
   * @return tags
   */
  def getAll: IO[Seq[ResponseTag]] = {

    def makeRequest(): (GetAll, Seq[ResponseTag] => Seq[ResponseTag]) = {
      (GetAll(), Seq[ResponseTag])
    }

    def run(): IO[Seq[ResponseTag]] = {
      val (request, cont) = makeRequest()
      TagRepository.dispatch(request).transact(doobieContext.transactor)
    }

    run()
  }

  /**
   * find tag by tagName
   *
   * @param tagName
   * @return maybe Tag
   */
  def findByName(tagName: TagName): IO[Option[Tag]] = {

    def makeRequest(tagName: TagName): (FindByName, Option[Tag] => Option[Tag]) = {
      val request = FindByName(tagName)
      val cont: Option[Tag] => Option[Tag] = (maybeTag: Option[Tag]) => { maybeTag }
      (request, cont)
    }

    def run(tagName: TagName): IO[Option[Tag]] = {
      val (request, cont) = makeRequest(tagName)
      TagRepository.dispatch(request).transact(doobieContext.transactor)
    }

    run(tagName)
  }

  /**
   * find tag from db or create new instance (just create instance, no insert to DB)
   *
   * @param tagName
   * @return Tag instance
   */
  def findOrGetNewInstance(tagName: TagName): IO[Tag] = {
    this.findByName(tagName).flatMap {
      case None => IO(Tag(new TagId, tagName))
      case Some(t) => IO(t)
    }
  }

  /**
   * get tag from db or new instance
   *
   * @param tagNames
   * @return
   */
  def getTags(tagNames: Option[List[String]]): IO[Option[List[Tag]]] = {
    tagNames match {
      case None => IO(None)
      case Some(t) => t.map { t => findOrGetNewInstance(TagName(t)) }.sequence.option
    }
  }

  /**
   * create a Tag
   *
   * @param data List of Tag
   * @return dummy long id (Doobie return Int)
   *
   * TODO: avoid using ConnectionIO
   */
  def bulkUpsertWithoutTaransact(data: Option[List[Tag]]): ConnectionIO[Int] = {

    def makeRequest(data: Option[List[Tag]]): (BulkUpsert, ConnectionIO[Int] => ConnectionIO[Int]) = {
      val request = BulkUpsert(data)
      val cont: ConnectionIO[Int] => ConnectionIO[Int] = (connectionIO: ConnectionIO[Int]) => { connectionIO }
      (request, cont)
    }

    def run(data: Option[List[Tag]]): ConnectionIO[Int] = {
      val (request, cont) = makeRequest(data)
      TagRepository.dispatch(request)
    }

    run(data)
  }
}
