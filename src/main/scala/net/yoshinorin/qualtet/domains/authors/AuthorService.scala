package net.yoshinorin.qualtet.domains.authors

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.domains.models.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class AuthorService(authorRepository: AuthorRepository)(implicit doobieContext: DoobieContextBase) extends ServiceBase {

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[ResponseAuthor] = {
    for {
      _ <- authorRepository.upsert(data).transact(doobieContext.transactor)
      a <- findBy(data.name, InternalServerError("user not found"))(this.findByName)
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[ResponseAuthor]] = {
    authorRepository.getAll.transact(doobieContext.transactor)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[ResponseAuthor]] = {
    authorRepository.findById(id).transact(doobieContext.transactor)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {
    authorRepository.findByIdWithPassword(id).transact(doobieContext.transactor)
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[ResponseAuthor]] = {
    authorRepository.findByName(name).transact(doobieContext.transactor)
  }

}
