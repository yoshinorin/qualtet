package net.yoshinorin.qualtet.application.contentTypes

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, ContentTypeRepository}

class ContentTypeFinder(contentTypeRepository: ContentTypeRepository) {

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  def getAll: ConnectionIO[Seq[ContentType]] = {
    contentTypeRepository.getAll
  }

}
