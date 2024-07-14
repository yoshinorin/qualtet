package net.yoshinorin.qualtet.domains.contentTaggings

import cats.data.NonEmptyList
import doobie.{Read, Write}
import doobie.implicits.toSqlInterpolator
import doobie.util.update.{Update, Update0}
import doobie.util.{fragments, query}
import net.yoshinorin.qualtet.domains.tags.TagId
import net.yoshinorin.qualtet.domains.contents.ContentId

object ContentTaggingQuery {

  def findByTagId(id: TagId): Read[ContentTagging] ?=> query.Query0[ContentTagging] = {
    sql"SELECT * FROM contents_tagging FROM tag_id = ${id.value}"
      .query[ContentTagging]
  }

  def bulkUpsert: Write[ContentTagging] ?=> Update[ContentTagging] = {
    val q = s"""
          INSERT INTO contents_tagging (content_id, tag_id)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            content_id = VALUES(content_id),
            tag_id = VALUES(tag_id)
        """
    Update[ContentTagging](q)
  }

  def deleteByContentId(id: ContentId): Update0 = {
    sql"DELETE FROM contents_tagging WHERE content_id = ${id.value}".update
  }

  def deleteByTagId(id: TagId): Update0 = {
    sql"DELETE FROM contents_tagging WHERE tag_id = ${id.value}".update
  }

  def delete(contentId: ContentId, tagIds: Seq[TagId]): Update0 = {
    val query = fr"""
      DELETE FROM contents_tagging
      WHERE content_id = ${contentId.value}
      AND """ ++ fragments.in(fr"tag_id", NonEmptyList.fromList(tagIds.toList.map(t => t.value)).get)

    query.update
  }

}
