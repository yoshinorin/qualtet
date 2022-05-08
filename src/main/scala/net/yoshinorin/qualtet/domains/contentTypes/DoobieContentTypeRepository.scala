package net.yoshinorin.qualtet.domains.contentTypes

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contentTypes.RepositoryRequests._

class DoobieContentTypeRepository extends ContentTypeRepository {

  /**
   * create a ContentType
   *
   * @param request Upsert case class
   * @return created Content with ConnectionIO
   */
  def dispatch(request: Upsert): ConnectionIO[Int] = {
    DoobieContentTypeQuery.upsert.run(request.data)
  }

  /**
   * get all ContentTypes
   *
   * @param request GetAll case class
   * @return ContentTypes
   */
  override def dispatch(request: GetAll): ConnectionIO[Seq[ContentType]] = {
    DoobieContentTypeQuery.getAll.to[Seq]
  }

  /**
   * find a ContentType by name
   *
   * @param request FindByName case class
   * @return ContentType
   */
  override def dispatch(request: FindByName): ConnectionIO[Option[ContentType]] = {
    DoobieContentTypeQuery.findByName(request.name).option
  }
}
