package net.yoshinorin.qualtet.domains.authors

import doobie.{Read, Write}
import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update

object AuthorQuery {

  def upsert: Write[Author] ?=> Update[Author] = {
    val q = s"""
          INSERT INTO authors (id, name, display_name, password, created_at)
            VALUES (?, ?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE
            display_name = VALUES(display_name),
            password = VALUES(password)
        """
    Update[Author](q)
  }

  def getAll: Read[AuthorWithoutPasswordReadModel] ?=> Query0[AuthorWithoutPasswordReadModel] = {
    sql"SELECT id, name, display_name, created_at FROM authors"
      .query[AuthorWithoutPasswordReadModel]
  }

  def findById(id: AuthorId): Read[AuthorWithoutPasswordReadModel] ?=> Query0[AuthorWithoutPasswordReadModel] = {
    sql"SELECT id, name, display_name, created_at FROM authors where id = ${id.value}"
      .query[AuthorWithoutPasswordReadModel]
  }

  def findByIdWithPassword(id: AuthorId): Read[AuthorReadModel] ?=> Query0[AuthorReadModel] = {
    sql"SELECT id, name, display_name, password, created_at FROM authors where id = ${id.value}"
      .query[AuthorReadModel]
  }

  def findByName(name: AuthorName): Read[AuthorWithoutPasswordReadModel] ?=> Query0[AuthorWithoutPasswordReadModel] = {
    sql"SELECT id, name, display_name, created_at FROM authors where name = ${name.value}"
      .query[AuthorWithoutPasswordReadModel]
  }
}
