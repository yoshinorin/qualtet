package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.InternalServerError
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorRepository}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class AuthorService(authorRepository: AuthorRepository)(implicit doobieContext: DoobieContext) {

  /**
   * create an author
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[Author] = {

    def author: IO[Author] = this.findByName(data.name).flatMap {
      case None => IO.raiseError(InternalServerError) //NOTE: 404 is better?
      case Some(x) => IO(x)
    }

    for {
      _ <- authorRepository.upsert(data).transact(doobieContext.transactor)
      a <- author
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[Author]] = {
    authorRepository.getAll.transact(doobieContext.transactor)
  }

  /**
   * find an Author by name
   *
   * @param name author's name
   * @return Author
   */
  def findByName(name: String): IO[Option[Author]] = {
    authorRepository.findByName(name).transact(doobieContext.transactor)
  }

}
