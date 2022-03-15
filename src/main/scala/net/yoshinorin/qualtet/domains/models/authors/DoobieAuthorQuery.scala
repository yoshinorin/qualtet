package net.yoshinorin.qualtet.domains.models.authors

import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update

object DoobieAuthorQuery {

  def upsert: Update[Author] = {
    val q = s"""
          INSERT INTO authors (id, name, display_name, password, created_at)
            VALUES (?, ?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE
            display_name = VALUES(display_name),
            password = VALUES(password)
        """
    Update[Author](q)
  }

  def getAll: Query0[ResponseAuthor] = {
    sql"SELECT id, name, display_name, created_at FROM authors"
      .query[ResponseAuthor]
  }

  def findById(id: AuthorId): Query0[ResponseAuthor] = {
    sql"SELECT id, name, display_name, created_at FROM authors where id = $id"
      .query[ResponseAuthor]
  }

  def findByIdWithPassword(id: AuthorId): Query0[Author] = {
    sql"SELECT id, name, display_name, password, created_at FROM authors where id = $id"
      .query[Author]
  }

  def findByName(name: AuthorName): Query0[ResponseAuthor] = {
    sql"SELECT id, name, display_name, created_at FROM authors where name = $name"
      .query[ResponseAuthor]
  }
}
