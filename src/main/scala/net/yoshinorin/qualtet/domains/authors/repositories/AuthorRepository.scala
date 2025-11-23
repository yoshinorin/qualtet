package net.yoshinorin.qualtet.domains.authors

trait AuthorRepository[F[_]] {

  /**
   * get all Author
   *
   * @return Authors
   */
  def getAll(): F[Seq[AuthorWithoutPasswordReadModel]]

  /**
   * create a authorName
   *
   * @param data Instance of AuthorWriteModel
   * @return dummy long id (Doobie return Int)
   */
  def upsert(data: AuthorWriteModel): F[Int]

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): F[Option[AuthorWithoutPasswordReadModel]]

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): F[Option[AuthorReadModel]]

  /**
   * find a Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): F[Option[AuthorWithoutPasswordReadModel]]
}

object AuthorRepository {

  import doobie.{Read, Write}
  import doobie.ConnectionIO

  given AuthorRepository: AuthorRepository[ConnectionIO] = {
    new AuthorRepository[ConnectionIO] {

      given authorWithoutPasswordRead: Read[AuthorWithoutPasswordReadModel] =
        Read[(String, String, String, Long)].map { case (id, name, displayName, createdAt) =>
          AuthorWithoutPasswordReadModel(
            AuthorId(id),
            AuthorName.fromTrusted(name),
            AuthorDisplayName.fromTrusted(displayName),
            createdAt
          )
        }

      given authorWithoutPasswordOrOptionRead: Read[Option[AuthorWithoutPasswordReadModel]] =
        Read[(String, String, String, Long)].map { case (id, name, displayName, createdAt) =>
          Some(
            AuthorWithoutPasswordReadModel(
              AuthorId(id),
              AuthorName.fromTrusted(name),
              AuthorDisplayName.fromTrusted(displayName),
              createdAt
            )
          )
        }

      given authorRead: Read[AuthorReadModel] =
        Read[(String, String, String, String, Long)].map { case (id, name, displayName, password, createdAt) =>
          AuthorReadModel(
            AuthorId(id),
            AuthorName.fromTrusted(name),
            AuthorDisplayName.fromTrusted(displayName),
            BCryptPassword(password),
            createdAt
          )
        }

      given authorOrOptionRead: Read[Option[AuthorReadModel]] =
        Read[(String, String, String, String, Long)].map { case (id, name, displayName, password, createdAt) =>
          Some(
            AuthorReadModel(
              AuthorId(id),
              AuthorName.fromTrusted(name),
              AuthorDisplayName.fromTrusted(displayName),
              BCryptPassword(password),
              createdAt
            )
          )
        }

      given authorWrite: Write[AuthorWriteModel] =
        Write[(String, String, String, String, Long)]
          .contramap(a => (a.id.value, a.name.value, a.displayName.value, a.password.value, a.createdAt))

      override def getAll(): ConnectionIO[Seq[AuthorWithoutPasswordReadModel]] = AuthorQuery.getAll.to[Seq]

      // TODO: Do not do `run` here
      override def upsert(data: AuthorWriteModel): ConnectionIO[Int] = AuthorQuery.upsert.run(data)

      override def findById(id: AuthorId): ConnectionIO[Option[AuthorWithoutPasswordReadModel]] = AuthorQuery.findById(id).option

      override def findByIdWithPassword(id: AuthorId): ConnectionIO[Option[AuthorReadModel]] = AuthorQuery.findByIdWithPassword(id).option

      override def findByName(name: AuthorName): ConnectionIO[Option[AuthorWithoutPasswordReadModel]] = AuthorQuery.findByName(name).option

    }
  }

}
