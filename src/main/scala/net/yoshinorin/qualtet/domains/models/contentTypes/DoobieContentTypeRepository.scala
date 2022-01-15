package net.yoshinorin.qualtet.domains.models.contentTypes

import doobie.{ConnectionIO, Update}
import doobie.implicits._

class DoobieContentTypeRepository extends ContentTypeRepository {

  /**
   * create a ContentType
   *
   * @param data Instance of ContentType
   * @return created Content with ConnectionIO
   */
  def upsert(data: ContentType): ConnectionIO[Int] = {
    val q = s"""
          INSERT INTO content_types (id, name)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            name = VALUES(name)
        """
    Update[ContentType](q).run(data)
  }

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
