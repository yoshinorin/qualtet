package net.yoshinorin.qualtet.domains.tags

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.tags.RepositoryRequests.{BulkUpsert, FindByName, GetAll}
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker

class DoobieTagRepository extends TagRepository with ConnectionIOFaker {

  def dispatch(requests: GetAll): ConnectionIO[Seq[ResponseTag]] = {
    DoobieTagQuery.getAll.to[Seq]
  }

  /**
   * find a Tag by Name
   *
   * @param request FindByName request
   * @return dummy long id (Doobie return Int)
   */
  def dispatch(request: FindByName): ConnectionIO[Option[Tag]] = {
    DoobieTagQuery.findByName(request.data).option
  }

  /**
   * create a Tag
   *
   * @param request BulkUpsert request object
   * @return dummy long id (Doobie return Int)
   *
   * TODO: return ConnectionIO[Long]
   */
  def dispatch(request: BulkUpsert): ConnectionIO[Int] = {
    request.data match {
      case None => ConnectionIOWithInt
      case Some(x) =>
        DoobieTagQuery.bulkUpsert.updateMany(x)
    }
  }

}
