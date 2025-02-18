package net.yoshinorin.qualtet.domains.contentTypes

import doobie.{Read, Write}
import doobie.syntax.all.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update

object ContentTypeQuery {

  def upsert: Write[ContentTypeWriteModel] ?=> Update[ContentTypeWriteModel] = {
    val q = s"""
          INSERT INTO content_types (id, name)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            name = VALUES(name)
        """
    Update[ContentTypeWriteModel](q)
  }

  def getAll: Read[ContentTypeReadModel] ?=> Query0[ContentTypeReadModel] = {
    sql"SELECT * FROM content_types"
      .query[ContentTypeReadModel]
  }

  def findByName(name: String): Read[ContentTypeReadModel] ?=> Query0[ContentTypeReadModel] = {
    sql"SELECT * FROM content_types WHERE name = ${name}"
      .query[ContentTypeReadModel]
  }
}
