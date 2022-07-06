package net.yoshinorin.qualtet.domains.authors

import cats.effect.IO
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.domains.ServiceLogic._
import net.yoshinorin.qualtet.domains.{ServiceLogic, Continue, Done}

class AuthorService()(doobieContext: DoobieContextBase) extends ServiceBase {

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[ResponseAuthor] = {

    def procedures(data: Author): ServiceLogic[Int] = {
      val request = Upsert(data)
      val resultHandler: Int => ServiceLogic[Int] = (resultHandler: Int) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    for {
      _ <- procedures(data).transact()(doobieContext)
      a <- findBy(data.name, InternalServerError("user not found"))(this.findByName)
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[ResponseAuthor]] = {

    def procedures(): ServiceLogic[Seq[ResponseAuthor]] = {
      val request = GetAll()
      val resultHandler: Seq[ResponseAuthor] => ServiceLogic[Seq[ResponseAuthor]] = (resultHandler: Seq[ResponseAuthor]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    procedures().transact()(doobieContext)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[ResponseAuthor]] = {

    def procedures(id: AuthorId): ServiceLogic[Option[ResponseAuthor]] = {
      val request = FindById(id)
      val resultHandler: Option[ResponseAuthor] => ServiceLogic[Option[ResponseAuthor]] = (resultHandler: Option[ResponseAuthor]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    procedures(id).transact()(doobieContext)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {

    def procedures(id: AuthorId): ServiceLogic[Option[Author]] = {
      val request = FindByIdWithPassword(id)
      val resultHandler: Option[Author] => ServiceLogic[Option[Author]] = (resultHandler: Option[Author]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    procedures(id).transact()(doobieContext)
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[ResponseAuthor]] = {

    def procedures(name: AuthorName): ServiceLogic[Option[ResponseAuthor]] = {
      val request = FindByName(name)
      val resultHandler: Option[ResponseAuthor] => ServiceLogic[Option[ResponseAuthor]] = { resultHandler: Option[ResponseAuthor] => Done(resultHandler) }
      Continue(request, resultHandler)
    }

    transact(procedures(name))(doobieContext)
  }

}
