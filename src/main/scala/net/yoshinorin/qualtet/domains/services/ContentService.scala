package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import net.yoshinorin.qualtet.application.contents.{ContentCreator, ContentFinder}
import net.yoshinorin.qualtet.domains.models.contents.Content

class ContentService(contentFinder: ContentFinder, contentCreator: ContentCreator) {

  def create(data: Content): IO[Content] = {
    contentCreator.create(data)
  }

  def getAll: IO[Seq[Content]] = {
    contentFinder.getAll
  }

}
