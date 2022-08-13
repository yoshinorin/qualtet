package net.yoshinorin.qualtet.domains.contents

import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update

object ContentQuery {

  def upsert: Update[Content] = {
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

  def delete(id: ContentId): Query0[Unit] = {
    sql"DELETE FROM contents WHERE id = ${id.value}"
      .query[Unit]
  }

  def findByPath(path: Path): Query0[Content] = {
    sql"SELECT * FROM contents WHERE path = $path"
      .query[Content]
  }

  def findByPathWithMeta(path: Path): Query0[Option[ResponseContentDbRow]] = {
    // NOTE: Do not use `.option` use `.query[Option[T]].unique` instead
    //       https://stackoverflow.com/questions/57873699/sql-null-read-at-column-1-jdbc-type-null-but-mapping-is-to-a-non-option-type
    sql"""
       SELECT
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
         path = $path
    """
      .query[Option[ResponseContentDbRow]]
  }

}
