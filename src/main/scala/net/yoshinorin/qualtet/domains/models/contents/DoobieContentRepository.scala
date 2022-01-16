package net.yoshinorin.qualtet.domains.models.contents

import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.update.Update

class DoobieContentRepository extends ContentRepository {

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return created Content with ConnectionIO
   */
  def upsert(data: Content): ConnectionIO[Int] = {
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
    Update[Content](q).run(data)
  }

  /**
   * find a content by path of content
   *
   * @param path path of content
   * @return Content with ConnectionIO
   */
  def findByPath(path: Path): ConnectionIO[Option[Content]] = {
    sql"SELECT * FROM contents WHERE path = $path"
      .query[Content]
      .option
  }

  /**
   * find a content by path of content
   *
   * @param path path of content
   * @return Content with ConnectionIO
   */
  def findByPathWithMeta(path: Path): ConnectionIO[Option[ResponseContentDbRow]] = {
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
         published_at
       FROM
         contents
       INNER JOIN robots ON
         contents.id = robots.content_id
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
      .unique
  }
}
