package net.yoshinorin.qualtet.domains.models.contents

import doobie.ConnectionIO

trait ContentRepository {

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return dummy long (Doobie return Long)
   */
  def insert(data: Content): ConnectionIO[Long]

  def find = ???

  def findByPath(path: String): ConnectionIO[Content]

  def update = ???

  def getAll: ConnectionIO[Seq[Content]]
}
