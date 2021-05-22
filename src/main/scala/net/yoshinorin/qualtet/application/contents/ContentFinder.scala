package net.yoshinorin.qualtet.application.contents

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentRepository}

class ContentFinder(contentRepisitory: ContentRepository) {

  def getAll: ConnectionIO[Seq[Content]] = {
    contentRepisitory.getAll
  }

  def findByPath(path: String): ConnectionIO[Content] = {
    contentRepisitory.findByPath(path)
  }

}
