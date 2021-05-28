package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.application.authors.AuthorFinder
import net.yoshinorin.qualtet.domains.models.authors.Author
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class AuthorService(authorFinder: AuthorFinder)(implicit doobieContext: DoobieContext) {

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[Author]] = {
    authorFinder.getAll.transact(doobieContext.transactor)
  }

  /**
   * find an Author by name
   *
   * @param name author's name
   * @return Author
   */
  def findByName(name: String): IO[Option[Author]] = {
    authorFinder.findByName(name).transact(doobieContext.transactor)
  }

}
