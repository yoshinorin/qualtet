package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.application.contentTypes.ContentTypeFinder
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentType
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class ContentTypeService(contentTypeFinder: ContentTypeFinder)(implicit doobieContext: DoobieContext) {

  /**
   * get all ContentTypes
   *
   * @return ContentTypes
   */
  def getAll: IO[Seq[ContentType]] = {
    contentTypeFinder.getAll.transact(doobieContext.transactor)
  }

}
