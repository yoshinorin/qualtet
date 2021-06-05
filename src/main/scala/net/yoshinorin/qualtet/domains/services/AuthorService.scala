package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorRepository}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class AuthorService(authorRepository: AuthorRepository)(implicit doobieContext: DoobieContext) {

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
