package net.yoshinorin.qualtet.domains.authors

import cats.effect.IO
import doobie.ConnectionIO
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.domains.DoobieAction._
import net.yoshinorin.qualtet.domains.{DoobieAction, DoobieContinue}
import net.yoshinorin.qualtet.syntax._

class AuthorService(
  authorRepository: AuthorRepository[ConnectionIO]
)(doobieContext: DoobieContext) {

  def upsertActions(data: Author): DoobieAction[Int] = {
    DoobieContinue(authorRepository.upsert(data), DoobieAction.buildDoneWithoutAnyHandle[Int])
  }

  def fetchActions: DoobieAction[Seq[ResponseAuthor]] = {
    DoobieContinue(authorRepository.getAll(), DoobieAction.buildDoneWithoutAnyHandle[Seq[ResponseAuthor]])
  }

  def findByIdActions(id: AuthorId): DoobieAction[Option[ResponseAuthor]] = {
    DoobieContinue(authorRepository.findById(id), DoobieAction.buildDoneWithoutAnyHandle[Option[ResponseAuthor]])
  }

  def findByIdWithPasswordActions(id: AuthorId): DoobieAction[Option[Author]] = {
    DoobieContinue(authorRepository.findByIdWithPassword(id), DoobieAction.buildDoneWithoutAnyHandle[Option[Author]])
  }

  def findByNameActions(name: AuthorName): DoobieAction[Option[ResponseAuthor]] = {
    DoobieContinue(authorRepository.findByName(name), DoobieAction.buildDoneWithoutAnyHandle[Option[ResponseAuthor]])
  }

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[ResponseAuthor] = {
    for {
      _ <- upsertActions(data).perform.andTransact(doobieContext)
      a <- this.findByName(data.name).throwIfNone(InternalServerError("user not found"))
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[ResponseAuthor]] = {
    fetchActions.perform.andTransact(doobieContext)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[ResponseAuthor]] = {
    findByIdActions(id).perform.andTransact(doobieContext)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {
    findByIdWithPasswordActions(id).perform.andTransact(doobieContext)
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[ResponseAuthor]] = {
    findByNameActions(name).perform.andTransact(doobieContext)
  }

}
