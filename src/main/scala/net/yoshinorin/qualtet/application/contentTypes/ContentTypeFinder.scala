package net.yoshinorin.qualtet.application.contentTypes

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, ContentTypeRepository}

class ContentTypeFinder(contentTypeRepository: ContentTypeRepository) {

  /**
   * find a ContentType by name
   *
   * @param name name of ContentType
   * @return ContentType
   */
  def findByName(name: String): ConnectionIO[Option[ContentType]] = {
    contentTypeRepository.findByName(name)
  }

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  def getAll: ConnectionIO[Seq[ContentType]] = {
    contentTypeRepository.getAll
  }

}
