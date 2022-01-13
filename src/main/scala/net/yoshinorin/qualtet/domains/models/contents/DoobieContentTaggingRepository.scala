package net.yoshinorin.qualtet.domains.models.contents

import doobie.ConnectionIO
import doobie.util.update.Update
import net.yoshinorin.qualtet.infrastructure.db.doobie.{ConnectionIOFaker, DoobieContextBase}

class DoobieContentTaggingRepository(doobie: DoobieContextBase) extends ContentTaggingRepository with ConnectionIOFaker {

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
        val q = s"""
          INSERT INTO contents_tagging (content_id, tag_id)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            content_id = VALUES(content_id),
            tag_id = VALUES(tag_id)
        """
        Update[ContentTagging](q)
          .updateMany(x)
    }
  }

}
