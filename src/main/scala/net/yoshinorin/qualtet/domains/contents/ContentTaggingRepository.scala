package net.yoshinorin.qualtet.domains.contents

import doobie.ConnectionIO

trait ContentTaggingRepository {

  /**
   * create a ContentTagging bulky
   *
   * @param data List of ContentTagging
   * @return dummy long id (Doobie return Int)
   *
   * TODO: remove Option
   * TODO: return ConnectionIO[Long]
   */
  def bulkUpsert(data: Option[List[ContentTagging]]): ConnectionIO[Int]

}
