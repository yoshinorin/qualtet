package net.yoshinorin.qualtet.domains.contentTaggings

import cats.data.NonEmptyList
import doobie.{Read, Write}
import doobie.implicits.toSqlInterpolator
import doobie.util.update.Update
import doobie.util.query
import net.yoshinorin.qualtet.domains.tags.TagId
import net.yoshinorin.qualtet.domains.contents.ContentId

object ContentTaggingQuery {

  def findByTagId(id: TagId)(implicit contentTaggingRead: Read[ContentTagging]): query.Query0[ContentTagging] = {
    sql"SELECT * FROM contents_tagging FROM tag_id = ${id.value}"
      .query[ContentTagging]
  }

  def bulkUpsert(implicit contentTaggingWrite: Write[ContentTagging]): Update[ContentTagging] = {
    val q = s"""
          INSERT INTO contents_tagging (content_id, tag_id)
            VALUES (?, ?)
          ON DUPLICATE KEY UPDATE
            content_id = VALUES(content_id),
            tag_id = VALUES(tag_id)
        """
    Update[ContentTagging](q)
  }

  def deleteByContentId(id: ContentId): query.Query0[Unit] = {
    sql"DELETE FROM contents_tagging WHERE content_id = ${id.value}"
      .query[Unit]
  }

  def deleteByTagId(id: TagId): query.Query0[Unit] = {
    sql"DELETE FROM contents_tagging WHERE tag_id = ${id.value}"
      .query[Unit]
  }

  def delete(contentId: ContentId, tagIds: Seq[TagId]): query.Query0[Unit] = {
    // https://tpolecat.github.io/doobie/docs/05-Parameterized.html#dealing-with-in-clauses
    // https://tpolecat.github.io/doobie/docs/08-Fragments.html#composing-sql-literals
    //
    // val inClauseFragment = fr" tag_id IN (" ++ tagIds.map(n => fr"$n").intercalate(fr",") ++ fr")"
    // val sTagIds = tagIds.map(t => s"${t.value}").mkString(",")
    // val inClauseFragment = fr" tag_id IN (" ++ fr"${sTagIds}" ++ fr")"
    // val inClauseFragment = doobie.util.fragments.in(fr"tag_id", NonEmptyList.fromList(tagIds.toList))

    (fr"""
      DELETE FROM contents_tagging
      WHERE content_id = ${contentId.value}
      AND """ ++ doobie.util.fragments.in(fr"tag_id", NonEmptyList.fromList(tagIds.toList.map(t => t.value)).get)).query[Unit]
  }

}
