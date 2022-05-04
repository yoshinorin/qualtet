package net.yoshinorin.qualtet.domains.authors

import doobie.ConnectionIO

class DoobieAuthorRepository extends AuthorRepository {

  /**
   * create a authorName
   *
   * @param data Instance of Author
   * @return created Author
   */
  override def upsert(data: Author): ConnectionIO[Int] = {
    DoobieAuthorQuery.upsert.run(data)
  }

  /**
   * get all Author
   *
   * @return Authors
   */
  override def getAll: ConnectionIO[Seq[ResponseAuthor]] = {
    DoobieAuthorQuery.getAll.to[Seq]
  }

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  override def findById(id: AuthorId): ConnectionIO[Option[ResponseAuthor]] = {
    DoobieAuthorQuery.findById(id).option
  }

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  override def findByIdWithPassword(id: AuthorId): ConnectionIO[Option[Author]] = {
    DoobieAuthorQuery.findByIdWithPassword(id).option
  }

  /**
   * find a Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  override def findByName(name: AuthorName): ConnectionIO[Option[ResponseAuthor]] = {
    DoobieAuthorQuery.findByName(name).option
  }
}
