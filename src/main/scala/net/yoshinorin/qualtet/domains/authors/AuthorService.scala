package net.yoshinorin.qualtet.domains.authors

import cats.effect.IO
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue, Done}

class AuthorService()(doobieContext: DoobieContextBase) extends ServiceBase {

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[ResponseAuthor] = {

    def actions(data: Author): Action[Int] = {
      val request = Upsert(data)
      val resultHandler: Int => Action[Int] = (resultHandler: Int) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    for {
      _ <- actions(data).perform.andTransact(doobieContext)
      a <- findBy(data.name, InternalServerError("user not found"))(this.findByName)
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[ResponseAuthor]] = {

    def actions: Action[Seq[ResponseAuthor]] = {
      val request = GetAll()
      val resultHandler: Seq[ResponseAuthor] => Action[Seq[ResponseAuthor]] = (resultHandler: Seq[ResponseAuthor]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    actions.perform.andTransact(doobieContext)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[ResponseAuthor]] = {

    def actions(id: AuthorId): Action[Option[ResponseAuthor]] = {
      val request = FindById(id)
      val resultHandler: Option[ResponseAuthor] => Action[Option[ResponseAuthor]] = (resultHandler: Option[ResponseAuthor]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    actions(id).perform.andTransact(doobieContext)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {

    def actions(id: AuthorId): Action[Option[Author]] = {
      val request = FindByIdWithPassword(id)
      val resultHandler: Option[Author] => Action[Option[Author]] = (resultHandler: Option[Author]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    actions(id).perform.andTransact(doobieContext)
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[ResponseAuthor]] = {

    def actions(name: AuthorName): Action[Option[ResponseAuthor]] = {
      val request = FindByName(name)
      val resultHandler: Option[ResponseAuthor] => Action[Option[ResponseAuthor]] = { resultHandler: Option[ResponseAuthor] => Done(resultHandler) }
      Continue(request, resultHandler)
    }

    actions(name).perform.andTransact(doobieContext)
  }

}
