package net.yoshinorin.qualtet.domains.contents

import doobie.{Read, Write}
import doobie.syntax.all.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.{Update, Update0}
import net.yoshinorin.qualtet.domains.contents.ContentPath

object ContentQuery {

  def upsert: Write[ContentWriteModel] ?=> Update[ContentWriteModel] = {
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
    Update[ContentWriteModel](q)
  }

  def delete(id: ContentId): Update0 = {
    sql"DELETE FROM contents WHERE id = ${id.value}".update
  }

  def findById(id: ContentId): Read[ContentReadModel] ?=> Query0[ContentReadModel] = {
    sql"SELECT * FROM contents WHERE id = ${id.value}"
      .query[ContentReadModel]
  }

  def findByPath(path: ContentPath): Read[ContentReadModel] ?=> Query0[ContentReadModel] = {
    sql"SELECT * FROM contents WHERE path = ${path.value}"
      .query[ContentReadModel]
  }

  def findByPathWithMeta(path: ContentPath): Read[ContentWithMetaReadModel] ?=> Query0[ContentWithMetaReadModel] = {
    sql"""
       SELECT
         contents.id AS id,
         title,
         robots.attributes AS robotsAttributes,
         GROUP_CONCAT(external_resources.kind) AS externalResourceKindKey,
         GROUP_CONCAT(external_resources.name) AS externalResourceKindValue,
         GROUP_CONCAT(tags.id) AS tagId,
         GROUP_CONCAT(tags.name) AS tagName,
         GROUP_CONCAT(tags.path) AS tagPath,
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
         contents.path = ${path.value}
       HAVING
   	     COUNT(*) > 0
    """
      .query[ContentWithMetaReadModel]
  }

  def findAdjacent(id: ContentId): Read[(Option[AdjacentContentModel], Option[AdjacentContentModel])] ?=> Query0[
    (Option[AdjacentContentModel], Option[AdjacentContentModel])
  ] = {
    sql"""
       SELECT
         prev_id,
         prev_path,
         prev_title,
         prev_published_at,
         next_id,
         next_path,
         next_title,
         next_published_at
       FROM (
         SELECT
           contents.id,
           LAG(contents.id) OVER (ORDER BY contents.published_at) AS prev_id,
           LAG(contents.path) OVER (ORDER BY contents.published_at) AS prev_path,
           LAG(contents.title) OVER (ORDER BY contents.published_at) AS prev_title,
           LAG(contents.published_at) OVER (ORDER BY contents.published_at) AS prev_published_at,
           LEAD(contents.id) OVER (ORDER BY contents.published_at) AS next_id,
           LEAD(contents.path) OVER (ORDER BY contents.published_at) AS next_path,
           LEAD(contents.title) OVER (ORDER BY contents.published_at) AS next_title,
           LEAD(contents.published_at) OVER (ORDER BY contents.published_at) AS next_published_at
         FROM contents
         INNER JOIN content_types ON contents.content_type_id = content_types.id
         WHERE content_types.name = 'article'
       ) windowed
       WHERE windowed.id = ${id.value}
    """
      .query[(Option[AdjacentContentModel], Option[AdjacentContentModel])]
  }

}
