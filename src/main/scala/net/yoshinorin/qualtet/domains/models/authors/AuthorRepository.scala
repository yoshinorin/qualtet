package net.yoshinorin.qualtet.domains.models.authors

import doobie.ConnectionIO

trait AuthorRepository {

  /**
   * create a author
   *
   * @param data Instance of Author
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: Author): ConnectionIO[Long]

  /**
   * get all Author
   *
   *
   * @return Authors
   */
  def getAll: ConnectionIO[Seq[ResponseAuthor]]

  /**
   * find a Author by id
   *
   * @param id author's id
   * @return Author
   */
  def findById(id: String): ConnectionIO[Option[ResponseAuthor]]

  /**
   * find a Author by id
   *
   * @param id author's id
   * @return Author
   */
  def findByIdWithPassword(id: String): ConnectionIO[Option[Author]]

  /**
   * find a Author by name
   *
   * @param name author's name
   * @return Author
   */
  def findByName(name: String): ConnectionIO[Option[ResponseAuthor]]
}
