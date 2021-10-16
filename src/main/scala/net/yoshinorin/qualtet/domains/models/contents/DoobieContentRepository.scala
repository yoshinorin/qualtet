package net.yoshinorin.qualtet.domains.models.contents

import doobie.ConnectionIO
import doobie.implicits._
import io.getquill.{idiom => _}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieContentRepository(doobie: DoobieContext) extends ContentRepository {

  import doobie.ctx._

  private val contents = quote(querySchema[Content]("contents"))

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return created Content with ConnectionIO
   */
  def upsert(data: Content): ConnectionIO[Long] = {
    val q = quote(
      contents
        .insert(lift(data))
        .onConflictUpdate(
          (existingRow, newRow) => existingRow.path -> (newRow.path),
          (existingRow, newRow) => existingRow.title -> (newRow.title),
          (existingRow, newRow) => existingRow.contentTypeId -> (newRow.contentTypeId),
          (existingRow, newRow) => existingRow.rawContent -> (newRow.rawContent),
          (existingRow, newRow) => existingRow.htmlContent -> (newRow.htmlContent),
          (existingRow, newRow) => existingRow.publishedAt -> (newRow.publishedAt),
          (existingRow, newRow) => existingRow.updatedAt -> (newRow.updatedAt)
        )
    )
    run(q)
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
  def findByPathWithMeta(path: Path): ConnectionIO[Option[ResponseContent]] = {
    sql"""
       SELECT
         title,
         robots.attributes AS robotsAttributes,
         html_content AS content,
         published_at
       FROM
         contents
       INNER JOIN robots ON
         contents.id = robots.content_id
       WHERE
         path = $path
    """
      .query[ResponseContent]
      .option
  }

  /**
   * get all Contents
   *
   * @return contents with ConnectionIO
   * TODO: SQL should update
   */
  def getAll: ConnectionIO[Seq[Content]] = {
    sql"SELECT * FROM contents"
      .query[Content]
      .to[Seq]
  }
}
