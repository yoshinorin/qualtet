package net.yoshinorin.qualtet.domains.models.authors

import doobie.ConnectionIO

trait AuthorRepository {

  /**
   * create a author
   *
   * @param date Instance of Author
   * @return created Author
   */
  def insert(date: Author): Author

  /**
   * get all Author
   *
   *
   * @return Authors
   */
  def getAll: ConnectionIO[Seq[Author]]

}
