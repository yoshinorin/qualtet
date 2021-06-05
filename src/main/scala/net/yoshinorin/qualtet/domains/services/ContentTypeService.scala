package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, ContentTypeRepository}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class ContentTypeService(contentTypeRepository: ContentTypeRepository)(implicit doobieContext: DoobieContext) {

  /**
   * find a ContentType by name
   *
   * @param name name of ContentType
   * @return ContentType
   */
  def findByName(name: String): IO[Option[ContentType]] = {
    contentTypeRepository.findByName(name).transact(doobieContext.transactor)
  }

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  def getAll: IO[Seq[ContentType]] = {
    contentTypeRepository.getAll.transact(doobieContext.transactor)
  }

}
