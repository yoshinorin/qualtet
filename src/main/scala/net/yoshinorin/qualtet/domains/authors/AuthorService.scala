package net.yoshinorin.qualtet.domains.authors

import cats.effect.IO
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.syntax._

class AuthorService()(doobieContext: DoobieContextBase) {

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[ResponseAuthor] = {

    def actions(data: Author): Action[Int] = {
      Continue(Upsert(data), Action.buildNext[Int])
    }

    for {
      _ <- actions(data).perform.andTransact(doobieContext)
      a <- this.findByName(data.name).throwIfNone(InternalServerError("user not found"))
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[ResponseAuthor]] = {

    def actions: Action[Seq[ResponseAuthor]] = {
      Continue(GetAll(), Action.buildNext[Seq[ResponseAuthor]])
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
      Continue(FindById(id), Action.buildNext[Option[ResponseAuthor]])
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
      Continue(FindByIdWithPassword(id), Action.buildNext[Option[Author]])
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
      Continue(FindByName(name), Action.buildNext[Option[ResponseAuthor]])
    }

    actions(name).perform.andTransact(doobieContext)
  }

}
