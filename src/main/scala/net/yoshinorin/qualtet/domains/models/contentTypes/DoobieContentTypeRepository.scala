package net.yoshinorin.qualtet.domains.models.contentTypes

import doobie.ConnectionIO

class DoobieContentTypeRepository extends ContentTypeRepository {

  /**
   * create a ContentType
   *
   * @param data Instance of ContentType
   * @return created Content with ConnectionIO
   */
  def upsert(data: ContentType): ConnectionIO[Int] = {
    DoobieContentTypeQuery.upsert(data).run(data)
  }

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  override def getAll: ConnectionIO[Seq[ContentType]] = {
    DoobieContentTypeQuery.getAll.to[Seq]
  }

  /**
   * find a ContentType by name
   *
   * @param name name of ContentType
   * @return ContentType
   */
  override def findByName(name: String): ConnectionIO[Option[ContentType]] = {
    DoobieContentTypeQuery.findByName(name).option
  }
}
