package net.yoshinorin.qualtet.domains.models.contents

import doobie.ConnectionIO
import doobie.util.update.Update
import io.getquill.{idiom => _}
import net.yoshinorin.qualtet.infrastructure.db.doobie.{ConnectionIOFaker, DoobieContext}

class DoobieContentTaggingRepository(doobie: DoobieContext) extends ContentTaggingRepository with ConnectionIOFaker {

  import doobie.ctx._

  private val contentsTagging = quote(querySchema[ContentTagging]("contents_tagging"))

  /**
   * create a ContentTagging
   *
   * @param data Instance of ContentTagging
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: ContentTagging): ConnectionIO[Long] = {
    val q = quote(
      contentsTagging
        .insert(lift(data))
        .onConflictUpdate(
          (existingRow, newRow) => existingRow.ContentId -> (newRow.ContentId),
          (existingRow, newRow) => existingRow.TagId -> (newRow.TagId)
        )
    )
    run(q)
  }

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
