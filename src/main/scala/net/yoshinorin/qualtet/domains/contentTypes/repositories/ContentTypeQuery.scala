package net.yoshinorin.qualtet.domains.contentTypes

import org.typelevel.doobie.{Read, Write}
import org.typelevel.doobie.syntax.all.toSqlInterpolator
import org.typelevel.doobie.util.query.Query0
import org.typelevel.doobie.util.update.Update

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

  def findByName(name: ContentTypeName): Read[ContentTypeReadModel] ?=> Query0[ContentTypeReadModel] = {
    sql"SELECT * FROM content_types WHERE name = ${name.value}"
      .query[ContentTypeReadModel]
  }
}
