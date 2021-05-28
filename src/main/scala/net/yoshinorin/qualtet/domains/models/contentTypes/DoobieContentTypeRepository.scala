package net.yoshinorin.qualtet.domains.models.contentTypes

import doobie.ConnectionIO
import doobie.implicits._
import io.getquill.{idiom => _}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieContentTypeRepository(doobie: DoobieContext) extends ContentTypeRepository {

  import doobie.ctx._

  private val authors = quote(querySchema[ContentType]("content_types"))

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  override def getAll: ConnectionIO[Seq[ContentType]] = {
    sql"SELECT * FROM content_types"
      .query[ContentType]
      .to[Seq]
  }

  /**
   * find a ContentType by name
   *
   * @param name name of ContentType
   * @return ContentType
   */
  override def findByName(name: String): ConnectionIO[Option[ContentType]] = {
    sql"SELECT * FROM content_types WHERE name = $name"
      .query[ContentType]
      .option
  }
}
