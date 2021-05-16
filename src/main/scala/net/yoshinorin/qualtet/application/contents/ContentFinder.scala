package net.yoshinorin.qualtet.application.contents

import cats.effect.IO
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentRepository}

class ContentFinder(contentRepisitory: ContentRepository) {

  def getAll: IO[Seq[Content]] = {
    contentRepisitory.getAll
  }

}
