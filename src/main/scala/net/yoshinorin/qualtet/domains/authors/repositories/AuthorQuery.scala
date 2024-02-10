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

  def getAll: Read[ResponseAuthor] ?=> Query0[ResponseAuthor] = {
    sql"SELECT id, name, display_name, created_at FROM authors"
      .query[ResponseAuthor]
  }

  def findById(id: AuthorId): Read[ResponseAuthor] ?=> Query0[ResponseAuthor] = {
    sql"SELECT id, name, display_name, created_at FROM authors where id = ${id.value}"
      .query[ResponseAuthor]
  }

  def findByIdWithPassword(id: AuthorId): Read[Author] ?=> Query0[Author] = {
    sql"SELECT id, name, display_name, password, created_at FROM authors where id = ${id.value}"
      .query[Author]
  }

  def findByName(name: AuthorName): Read[ResponseAuthor] ?=> Query0[ResponseAuthor] = {
    sql"SELECT id, name, display_name, created_at FROM authors where name = ${name.value}"
      .query[ResponseAuthor]
  }
}
