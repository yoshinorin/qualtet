package net.yoshinorin.qualtet.domains.contents

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contents.RepositoryReqiests._

object ContentRepository {

  /**
   * create a content
   *
   * @param Upsert case class
   * @return created Content with ConnectionIO
   */
  def dispatch(request: Upsert): ConnectionIO[Int] = {
    ContentQuery.upsert.run(request.data)
  }

  /**
   * find a content by path of content
   *
   * @param FindByPath case class
   * @return Content with ConnectionIO
   */
  def dispatch(request: FindByPath): ConnectionIO[Option[Content]] = {
    ContentQuery.findByPath(request.path).option
  }

  /**
   * find a content by path of content
   *
   * @param FindByPathWithMeta case class
   * @return Content with ConnectionIO
   */
  def dispatch(request: FindByPathWithMeta): ConnectionIO[Option[ResponseContentDbRow]] = {
    ContentQuery.findByPathWithMeta(request.path).unique
  }
}
