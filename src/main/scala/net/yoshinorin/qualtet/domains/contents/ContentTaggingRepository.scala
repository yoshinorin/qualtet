package net.yoshinorin.qualtet.domains.contents

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker

object ContentTaggingRepository extends ConnectionIOFaker {

  /**
   * create a ContentTagging bulky
   *
   * @param data List of ContentTagging
   * @return dummy long id (Doobie return Int)
   *
   * TODO: remove Option
   * TODO: return ConnectionIO[Long]
   */
  def bulkUpsert(data: Option[List[ContentTagging]]): ConnectionIO[Int] = {
    data match {
      case None => ConnectionIOWithInt
      case Some(x) =>
        ContentTaggingQuery.bulkUpsert.updateMany(x)
    }
  }

}
