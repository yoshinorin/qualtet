package net.yoshinorin.qualtet.domains.contentTypes

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contentTypes.RepositoryRequests._

trait ContentTypeRepository {

  /**
   * upsert a contentType
   *
   * @param request Upsert case class
   * @return dummy int id (Doobie return Int)
   */
  def dispatch(request: Upsert): ConnectionIO[Int]

  /**
   * find a ContentType by name
   *
   * @param request FindByName case class
   * @return ContentType
   */
  def dispatch(request: FindByName): ConnectionIO[Option[ContentType]]

  /**
   * get all ContentTypes
   *
   * @param request GetAll case class
   * @return
   */
  def dispatch(request: GetAll): ConnectionIO[Seq[ContentType]]

}
