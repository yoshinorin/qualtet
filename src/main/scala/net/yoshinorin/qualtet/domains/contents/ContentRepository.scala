package net.yoshinorin.qualtet.domains.contents

import doobie.ConnectionIO

trait ContentRepository {

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return dummy long id (Doobie return Int)
   */
  def upsert(data: Content): ConnectionIO[Int]

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
