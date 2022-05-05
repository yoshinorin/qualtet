package net.yoshinorin.qualtet.domains.tags

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.tags.RepositoryRequests.{BulkUpsert, FindByName, GetAll}

trait TagRepository {

  /**
   * get all tags
   *
   * @param request GetAll request object
   * @return Tags
   */
  def dispatch(request: GetAll): ConnectionIO[Seq[ResponseTag]]

  /**
   * find a Tag by Name
   *
   * @param request FindByName request object
   * @return Instance of Tag
   */
  def dispatch(request: FindByName): ConnectionIO[Option[Tag]]

  /**
   * create a Tag
   *
   * @param request BulkUpsert request object
   * @return dummy long id (Doobie return Int)
   *
   * TODO: return ConnectionIO[Long]
   */
  def dispatch(request: BulkUpsert): ConnectionIO[Int]

}
