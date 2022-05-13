package net.yoshinorin.qualtet.domains.contents

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contents.RepositoryReqiests._

class DoobieContentRepository extends ContentRepository {

  /**
   * create a content
   *
   * @param Upsert case class
   * @return created Content with ConnectionIO
   */
  override def dispatch(request: Upsert): ConnectionIO[Int] = {
    DoobieContentQuery.upsert.run(request.data)
  }

  /**
   * find a content by path of content
   *
   * @param FindByPath case class
   * @return Content with ConnectionIO
   */
  override def dispatch(request: FindByPath): ConnectionIO[Option[Content]] = {
    DoobieContentQuery.findByPath(request.path).option
  }

  /**
   * find a content by path of content
   *
   * @param FindByPathWithMeta case class
   * @return Content with ConnectionIO
   */
  override def dispatch(request: FindByPathWithMeta): ConnectionIO[Option[ResponseContentDbRow]] = {
    DoobieContentQuery.findByPathWithMeta(request.path).unique
  }
}
