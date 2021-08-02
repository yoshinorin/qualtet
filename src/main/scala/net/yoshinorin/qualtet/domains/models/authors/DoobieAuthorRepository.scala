package net.yoshinorin.qualtet.domains.models.authors

import doobie.ConnectionIO
import doobie.implicits._
import io.getquill.{idiom => _}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieAuthorRepository(doobie: DoobieContext) extends AuthorRepository {

  import doobie.ctx._

  private val authors = quote(querySchema[Author]("authors"))

  /**
   * create a author
   *
   * @param data Instance of Author
   * @return created Author
   */
  override def upsert(data: Author): ConnectionIO[Long] = {
    val q = quote(
      authors
        .insert(lift(data))
        .onConflictUpdate((existingRow, newRow) => existingRow.displayName -> (newRow.displayName))
    )
    run(q)
  }

  /**
   * get all Author
   *
   * @return Authors
   */
  override def getAll: ConnectionIO[Seq[Author]] = {
    sql"SELECT * FROM authors"
      .query[Author]
      .to[Seq]
  }

  /**
   * find a Author by name
   *
   * @param name author's name
   * @return Author
   */
  override def findByName(name: String): ConnectionIO[Option[Author]] = {
    sql"SELECT * FROM authors where name = $name"
      .query[Author]
      .option
  }
}
