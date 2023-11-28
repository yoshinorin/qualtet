package net.yoshinorin.qualtet.domains.authors

trait AuthorRepository[F[_]] {

  /**
   * get all Author
   *
   * @return Authors
   */
  def getAll(): F[Seq[ResponseAuthor]]

  /**
   * create a authorName
   *
   * @param data Instance of Author
   * @return dummy long id (Doobie return Int)
   */
  def upsert(data: Author): F[Int]

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): F[Option[ResponseAuthor]]

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): F[Option[Author]]

  /**
   * find a Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): F[Option[ResponseAuthor]]
}
