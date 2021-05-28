package net.yoshinorin.qualtet.application.authors

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorRepository}

class AuthorFinder(authorRepository: AuthorRepository) {

  /**
   * get all Author
   *
   * @return Authors
   */
  def getAll: ConnectionIO[Seq[Author]] = {
    authorRepository.getAll
  }

  /**
   * find an Author by name
   *
   * @param name author's name
   * @return Author
   */
  def findByName(name: String): ConnectionIO[Option[Author]] = {
    authorRepository.findByName(name)
  }

}
