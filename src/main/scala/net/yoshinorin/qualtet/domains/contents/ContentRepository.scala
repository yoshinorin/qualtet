package net.yoshinorin.qualtet.domains.contents

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contents.RepositoryReqiests._

trait ContentRepository {

  /**
   * create a content
   *
   * @param Upsert case class
   * @return dummy long id (Doobie return Int)
   */
  def dispatch(request: Upsert): ConnectionIO[Int]

  /**
   * find content by path
   *
   * @param FindByPath case class
   * @return content
   */
  def dispatch(request: FindByPath): ConnectionIO[Option[Content]]

  /**
   * find content by path
   *
   * @param FindByPathWithMeta case class
   * @return content
   */
  def dispatch(request: FindByPathWithMeta): ConnectionIO[Option[ResponseContentDbRow]]
}
