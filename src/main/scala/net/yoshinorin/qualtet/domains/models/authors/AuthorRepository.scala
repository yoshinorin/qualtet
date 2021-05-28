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

  /**
   * find a Author by name
   *
   * @param name author's name
   * @return Author
   */
  def findByName(name: String): ConnectionIO[Option[Author]]
}
