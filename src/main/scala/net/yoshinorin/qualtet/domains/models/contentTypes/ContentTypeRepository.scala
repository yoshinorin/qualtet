package net.yoshinorin.qualtet.domains.models.contentTypes

import doobie.ConnectionIO

trait ContentTypeRepository {

  def findByName = ???

  /**
   * get all ContentTypes
   *
   * @return
   */
  def getAll: ConnectionIO[Seq[ContentType]]

}
