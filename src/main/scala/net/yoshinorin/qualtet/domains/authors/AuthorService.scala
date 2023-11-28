package net.yoshinorin.qualtet.domains.authors

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.Transactor
import net.yoshinorin.qualtet.actions.Action.*
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.syntax.*

class AuthorService[F[_]: Monad](
  authorRepository: AuthorRepository[F]
)(using transactor: Transactor[F]) {

  def upsertActions(data: Author): Action[Int] = {
    Continue(authorRepository.upsert(data), Action.done[Int])
  }

  def fetchActions: Action[Seq[ResponseAuthor]] = {
    Continue(authorRepository.getAll(), Action.done[Seq[ResponseAuthor]])
  }

  def findByIdActions(id: AuthorId): Action[Option[ResponseAuthor]] = {
    Continue(authorRepository.findById(id), Action.done[Option[ResponseAuthor]])
  }

  def findByIdWithPasswordActions(id: AuthorId): Action[Option[Author]] = {
    Continue(authorRepository.findByIdWithPassword(id), Action.done[Option[Author]])
  }

  def findByNameActions(name: AuthorName): Action[Option[ResponseAuthor]] = {
    Continue(authorRepository.findByName(name), Action.done[Option[ResponseAuthor]])
  }

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[ResponseAuthor] = {
    for {
      _ <- transactor.transact(upsertActions(data))
      a <- this.findByName(data.name).throwIfNone(InternalServerError("user not found"))
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[ResponseAuthor]] = {
    transactor.transact(fetchActions)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[ResponseAuthor]] = {
    transactor.transact(findByIdActions(id))
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {
    transactor.transact(findByIdWithPasswordActions(id))
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[ResponseAuthor]] = {
    transactor.transact(findByNameActions(name))
  }

}
