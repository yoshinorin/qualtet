package net.yoshinorin.qualtet.domains.models.authors

import doobie.ConnectionIO

trait AuthorRepository {

  /**
   * create a authorName
   *
   * @param data Instance of Author
   * @return dummy long id (Doobie return Int)
   */
  def upsert(data: Author): ConnectionIO[Int]

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
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): ConnectionIO[Option[ResponseAuthor]]

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): ConnectionIO[Option[Author]]

  /**
   * find a Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): ConnectionIO[Option[ResponseAuthor]]
}
