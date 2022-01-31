package net.yoshinorin.qualtet.domains.models.contents

import doobie.ConnectionIO

class DoobieContentRepository extends ContentRepository {

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return created Content with ConnectionIO
   */
  def upsert(data: Content): ConnectionIO[Int] = {
    DoobieContentQuery.upsert(data).run(data)
  }

  /**
   * find a content by path of content
   *
   * @param path path of content
   * @return Content with ConnectionIO
   */
  def findByPath(path: Path): ConnectionIO[Option[Content]] = {
    DoobieContentQuery.findByPath(path).option
  }

  /**
   * find a content by path of content
   *
   * @param path path of content
   * @return Content with ConnectionIO
   */
  def findByPathWithMeta(path: Path): ConnectionIO[Option[ResponseContentDbRow]] = {
    DoobieContentQuery.findByPathWithMeta(path).unique
  }
}
