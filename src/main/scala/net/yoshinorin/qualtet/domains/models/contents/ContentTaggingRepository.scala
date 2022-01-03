package net.yoshinorin.qualtet.domains.models.contents

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.tags.Tag

trait ContentTaggingRepository {

  /**
   * create a ContentTagging
   *
   * @param data Instance of ContentTagging
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: ContentTagging): ConnectionIO[Long]

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
