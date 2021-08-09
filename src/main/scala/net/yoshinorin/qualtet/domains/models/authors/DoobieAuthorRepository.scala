package net.yoshinorin.qualtet.domains.models.authors

import doobie.ConnectionIO
import doobie.implicits._
import io.getquill.{idiom => _}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieAuthorRepository(doobie: DoobieContext) extends AuthorRepository {

  import doobie.ctx._

  private val authors = quote(querySchema[Author]("authors"))

  /**
   * create a authorName
   *
   * @param data Instance of Author
   * @return created Author
   */
  override def upsert(data: Author): ConnectionIO[Long] = {
    val q = quote(
      authors
        .insert(lift(data))
        .onConflictUpdate(
          (existingRow, newRow) => existingRow.displayName -> (newRow.displayName),
          (existingRow, newRow) => existingRow.password -> (newRow.password)
        )
    )
    run(q)
  }

  /**
   * get all Author
   *
   * @return Authors
   */
  override def getAll: ConnectionIO[Seq[ResponseAuthor]] = {
    sql"SELECT id, name, display_name, created_at FROM authors"
      .query[ResponseAuthor]
      .to[Seq]
  }

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  override def findById(id: AuthorId): ConnectionIO[Option[ResponseAuthor]] = {
    sql"SELECT id, name, display_name, created_at FROM authors where id = $id"
      .query[ResponseAuthor]
      .option
  }

  /**
   * find a Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  override def findByIdWithPassword(id: AuthorId): ConnectionIO[Option[Author]] = {
    sql"SELECT id, name, display_name, password, created_at FROM authors where id = $id"
      .query[Author]
      .option
  }

  /**
   * find a Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  override def findByName(name: AuthorName): ConnectionIO[Option[ResponseAuthor]] = {
    sql"SELECT id, name, display_name, created_at FROM authors where name = $name"
      .query[ResponseAuthor]
      .option
  }
}
