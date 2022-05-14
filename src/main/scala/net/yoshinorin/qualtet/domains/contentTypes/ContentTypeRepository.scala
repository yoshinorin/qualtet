package net.yoshinorin.qualtet.domains.contentTypes

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contentTypes.RepositoryRequests._

object ContentTypeRepository {

  /**
   * create a ContentType
   *
   * @param request Upsert case class
   * @return created Content with ConnectionIO
   */
  def dispatch(request: Upsert): ConnectionIO[Int] = {
    ContentTypeQuery.upsert.run(request.data)
  }

  /**
   * get all ContentTypes
   *
   * @param request GetAll case class
   * @return ContentTypes
   */
  def dispatch(request: GetAll): ConnectionIO[Seq[ContentType]] = {
    ContentTypeQuery.getAll.to[Seq]
  }

  /**
   * find a ContentType by name
   *
   * @param request FindByName case class
   * @return ContentType
   */
  def dispatch(request: FindByName): ConnectionIO[Option[ContentType]] = {
    ContentTypeQuery.findByName(request.name).option
  }
}
