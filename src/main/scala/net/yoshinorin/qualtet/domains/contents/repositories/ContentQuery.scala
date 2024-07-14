package net.yoshinorin.qualtet.domains.contents

import doobie.{Read, Write}
import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.{Update, Update0}

object ContentQuery {

  def upsert: Write[Content] ?=> Update[Content] = {
    val q = s"""
          INSERT INTO contents (id, author_id, content_type_id, path, title, raw_content, html_content, published_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
          ON DUPLICATE KEY UPDATE
            content_type_id = VALUES(content_type_id),
            title = VALUES(title),
            raw_content = VALUES(raw_content),
            html_content = VALUES(html_content),
            published_at = VALUES(published_at),
            updated_at = VALUES(updated_at)
        """
    Update[Content](q)
  }

  def delete(id: ContentId): Update0 = {
    sql"DELETE FROM contents WHERE id = ${id.value}".update
  }

  def findById(id: ContentId): Read[Content] ?=> Query0[Content] = {
    sql"SELECT * FROM contents WHERE id = ${id.value}"
      .query[Content]
  }

  def findByPath(path: Path): Read[Content] ?=> Query0[Content] = {
    sql"SELECT * FROM contents WHERE path = ${path.value}"
      .query[Content]
  }

  def findByPathWithMeta(path: Path): Read[ReadContentDbRow] ?=> Query0[ReadContentDbRow] = {
    sql"""
       SELECT
         contents.id AS id,
         title,
         robots.attributes AS robotsAttributes,
         GROUP_CONCAT(external_resources.kind) AS externalResourceKindKey,
         GROUP_CONCAT(external_resources.name) AS externalResourceKindValue,
         GROUP_CONCAT(tags.id) AS tagId,
         GROUP_CONCAT(tags.name) AS tagName,
         html_content AS content,
         authors.display_name as authorName,
         published_at,
         updated_at
       FROM
         contents
       INNER JOIN robots ON
         contents.id = robots.content_id
       INNER JOIN authors ON
       	 contents.author_id = authors.id
       LEFT JOIN external_resources ON
         contents.id = external_resources.content_id
       LEFT JOIN contents_tagging ON
         contents.id = contents_tagging.content_id
       LEFT JOIN tags ON
         contents_tagging.tag_id = tags.id
       WHERE
         path = ${path.value}
       HAVING
   	     COUNT(*) > 0
    """
      .query[ReadContentDbRow]
  }

}
