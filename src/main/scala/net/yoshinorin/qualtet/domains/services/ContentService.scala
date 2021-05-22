package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.application.contents.{ContentCreator, ContentFinder}
import net.yoshinorin.qualtet.domains.models.contents.Content
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class ContentService(contentFinder: ContentFinder, contentCreator: ContentCreator)(implicit doobieContext: DoobieContext) {

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return Instance of created Content with IO
   */
  def create(data: Content): IO[Content] = {
    for {
      _ <- contentCreator.create(data).transact(doobieContext.transactor)
      c <- contentFinder.findByPath(data.path).transact(doobieContext.transactor)
    } yield c
  }

  /**
   * get all contents
   *
   * @return Instance of Contents with IO
   */
  def getAll: IO[Seq[Content]] = {
    contentFinder.getAll.transact(doobieContext.transactor)
  }

}
