package net.yoshinorin.qualtet.domains.authors

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.syntax._

class AuthorService(
  authorRepository: AuthorRepository[ConnectionIO]
)(dbContext: DataBaseContext[Aux[IO, Unit]]) {

  def upsertActions(data: Author): Action[Int] = {
    Continue(authorRepository.upsert(data), Action.buildDoneWithoutAnyHandle[Int])
  }

  def fetchActions: Action[Seq[ResponseAuthor]] = {
    Continue(authorRepository.getAll(), Action.buildDoneWithoutAnyHandle[Seq[ResponseAuthor]])
  }

  def findByIdActions(id: AuthorId): Action[Option[ResponseAuthor]] = {
    Continue(authorRepository.findById(id), Action.buildDoneWithoutAnyHandle[Option[ResponseAuthor]])
  }

  def findByIdWithPasswordActions(id: AuthorId): Action[Option[Author]] = {
    Continue(authorRepository.findByIdWithPassword(id), Action.buildDoneWithoutAnyHandle[Option[Author]])
  }

  def findByNameActions(name: AuthorName): Action[Option[ResponseAuthor]] = {
    Continue(authorRepository.findByName(name), Action.buildDoneWithoutAnyHandle[Option[ResponseAuthor]])
  }

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[ResponseAuthor] = {
    for {
      _ <- upsertActions(data).perform.andTransact(dbContext)
      a <- this.findByName(data.name).throwIfNone(InternalServerError("user not found"))
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[ResponseAuthor]] = {
    fetchActions.perform.andTransact(dbContext)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[ResponseAuthor]] = {
    findByIdActions(id).perform.andTransact(dbContext)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {
    findByIdWithPasswordActions(id).perform.andTransact(dbContext)
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[ResponseAuthor]] = {
    findByNameActions(name).perform.andTransact(dbContext)
  }

}
