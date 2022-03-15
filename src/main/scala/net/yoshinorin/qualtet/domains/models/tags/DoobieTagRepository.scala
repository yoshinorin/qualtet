package net.yoshinorin.qualtet.domains.models.tags

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker

class DoobieTagRepository extends TagRepository with ConnectionIOFaker {

  def getAll: ConnectionIO[Seq[ResponseTag]] = {
    DoobieTagQuery.getAll.to[Seq]
  }

  /**
   * find a Tag by Name
   *
   * @param data Instance of ExternalResource
   * @return dummy long id (Doobie return Int)
   */
  def findByName(data: TagName): ConnectionIO[Option[Tag]] = {
    DoobieTagQuery.findByName(data).option
  }

  /**
   * create a Tag
   *
   * @param data List of Tag
   * @return dummy long id (Doobie return Int)
   *
   * TODO: remove Option
   * TODO: return ConnectionIO[Long]
   */
  def bulkUpsert(data: Option[List[Tag]]): ConnectionIO[Int] = {
    data match {
      case None => ConnectionIOWithInt
      case Some(x) =>
        DoobieTagQuery.bulkUpsert.updateMany(x)
    }
  }

}
