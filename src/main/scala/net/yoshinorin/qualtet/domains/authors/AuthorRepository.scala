package net.yoshinorin.qualtet.domains.authors

import doobie.{Read, Write}
import doobie.ConnectionIO

trait AuthorRepository[M[_]] {

  /**
   * get all Author
   *
   * @return Authors
   */
  def getAll(): M[Seq[ResponseAuthor]]

  /**
   * create a authorName
   *
   * @param data Instance of Author
   * @return dummy long id (Doobie return Int)
   */
  def upsert(data: Author): M[Int]

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): M[Option[ResponseAuthor]]

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): M[Option[Author]]

  /**
   * find a Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): M[Option[ResponseAuthor]]
}

class DoobieAuthorRepository extends AuthorRepository[ConnectionIO] {

  implicit val responseAuthorRead: Read[ResponseAuthor] =
    Read[(String, String, String, Long)].map { case (id, name, displayName, createdAt) =>
      ResponseAuthor(AuthorId(id), AuthorName(name), AuthorDisplayName(displayName), createdAt)
    }

  implicit val responseAuthorWithOptionRead: Read[Option[ResponseAuthor]] =
    Read[(String, String, String, Long)].map { case (id, name, displayName, createdAt) =>
      Some(ResponseAuthor(AuthorId(id), AuthorName(name), AuthorDisplayName(displayName), createdAt))
    }

  implicit val authorRead: Read[Author] =
    Read[(String, String, String, String, Long)].map { case (id, name, displayName, password, createdAt) =>
      Author(AuthorId(id), AuthorName(name), AuthorDisplayName(displayName), BCryptPassword(password), createdAt)
    }

  implicit val authorWithOptionRead: Read[Option[Author]] =
    Read[(String, String, String, String, Long)].map { case (id, name, displayName, password, createdAt) =>
      Some(Author(AuthorId(id), AuthorName(name), AuthorDisplayName(displayName), BCryptPassword(password), createdAt))
    }

  implicit val responseAuthorWrite: Write[Author] =
    Write[(String, String, String, String, Long)]
      .contramap(a => (a.id.value, a.name.value, a.displayName.value, a.password.value, a.createdAt))

  override def getAll(): ConnectionIO[Seq[ResponseAuthor]] = AuthorQuery.getAll.to[Seq]

  // TODO: Do not do `run` here
  override def upsert(data: Author): ConnectionIO[Int] = AuthorQuery.upsert.run(data)

  override def findById(id: AuthorId): ConnectionIO[Option[ResponseAuthor]] = AuthorQuery.findById(id).option

  override def findByIdWithPassword(id: AuthorId): ConnectionIO[Option[Author]] = AuthorQuery.findByIdWithPassword(id).option

  override def findByName(name: AuthorName): ConnectionIO[Option[ResponseAuthor]] = AuthorQuery.findByName(name).option
}
