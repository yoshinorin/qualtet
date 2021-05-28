package net.yoshinorin.qualtet.domains.models.contentTypes

import doobie.ConnectionIO

trait ContentTypeRepository {

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
