package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import net.yoshinorin.qualtet.application.contents.ContentFinder
import net.yoshinorin.qualtet.domains.models.contents.Content

class ContentService(contentFinder: ContentFinder) {

  def getAll: IO[Seq[Content]] = {
    contentFinder.getAll
  }

}
