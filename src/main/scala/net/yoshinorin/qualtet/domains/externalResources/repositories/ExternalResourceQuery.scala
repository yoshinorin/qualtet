package net.yoshinorin.qualtet.domains.externalResources

import doobie.{Read, Write}
import doobie.syntax.all.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.{Update, Update0}
import net.yoshinorin.qualtet.domains.contents.ContentId

object ExternalResourceQuery {

  def bulkUpsert: Write[ExternalResourceWriteModel] ?=> Update[ExternalResourceWriteModel] = {
    val q = s"""
          INSERT INTO external_resources (content_id, kind, name)
            VALUES (?, ?, ?)
          ON DUPLICATE KEY UPDATE
            content_id = VALUES(content_id),
            kind = VALUES(kind),
            name = VALUES(name)
        """
    Update[ExternalResourceWriteModel](q)
  }

  def findByContentId(id: ContentId): Read[ExternalResourcesReadModel] ?=> Query0[ExternalResourcesReadModel] = {
    sql"""
      SELECT external_resources.*
      FROM external_resources
      WHERE content_id = ${id.value}
    """.query[ExternalResourcesReadModel]
  }

  def delete(id: ContentId): Update0 = {
    sql"DELETE FROM external_resources WHERE content_id = ${id.value}".update
  }

  def bulkDelete(data: List[ExternalResourceDeleteModel]): Update0 = {
    val orClauseFragment =
      data.map(d => fr"(content_id = ${d.contentId.value} AND kind = ${d.kind.value} AND name = ${d.name})").reduce((a, b) => a ++ fr" OR " ++ b)

    /**
     * SQL example
     *
     * DELETE FROM external_resources
     * WHERE (content_id = "a" AND kind = "b1" AND name = "c1")
     * OR (content_id = "a" AND kind = "b1" AND name = "c2")
     * OR (content_id = "a" AND kind = "b2" AND name = "c1");
     */
    sql"DELETE FROM external_resources WHERE ${orClauseFragment}".update
  }

}
