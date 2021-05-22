package net.yoshinorin.qualtet.application.contents

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentRepository}

class ContentFinder(contentRepisitory: ContentRepository) {

  /**
   * Create a Content
   *
   * @return Instance of Content with ConnectionIO
   */
  def getAll: ConnectionIO[Seq[Content]] = {
    contentRepisitory.getAll
  }

  /**
   * find a Content by path
   *
   * @param path path of Content
   * @return Instance of Content with ConnectionIO
   */
  def findByPath(path: String): ConnectionIO[Content] = {
    contentRepisitory.findByPath(path)
  }

}
