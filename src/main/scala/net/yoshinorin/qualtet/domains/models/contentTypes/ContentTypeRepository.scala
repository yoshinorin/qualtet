package net.yoshinorin.qualtet.domains.models.contentTypes

import doobie.ConnectionIO

trait ContentTypeRepository {

  /**
   * upsert a contentType
   *
   * @param data Instance of contentType
   * @return dummy int id (Doobie return Int)
   */
  def upsert(data: ContentType): ConnectionIO[Int]

  /**
   * find a ContentType by name
   *
   * @param name name of ContentType
   * @return ContentType
   */
  def findByName(name: String): ConnectionIO[Option[ContentType]]

  /**
   * get all ContentTypes
   *
   * @return
   */
  def getAll: ConnectionIO[Seq[ContentType]]

}
