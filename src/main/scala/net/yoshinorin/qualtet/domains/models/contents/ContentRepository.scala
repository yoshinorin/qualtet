package net.yoshinorin.qualtet.domains.models.contents

import doobie.ConnectionIO

trait ContentRepository {

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: Content): ConnectionIO[Long]

  def find = ???

  /**
   * find content by path
   *
   * @param path path of content
   * @return content
   */
  def findByPath(path: Path): ConnectionIO[Option[Content]]

  /**
   * find content by path
   *
   * @param path path of content
   * @return content
   */
  def findByPathWithMeta(path: Path): ConnectionIO[Option[ResponseContentDbRow]]
}
