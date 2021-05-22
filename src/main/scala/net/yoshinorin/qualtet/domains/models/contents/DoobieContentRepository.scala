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
  def insert(data: Content): ConnectionIO[Long] = {
    val q = quote(contents.insert(lift(data)))
    run(q)
  }

  /**
   * find a content by path of content
   *
   * @param path path of content
   * @return Content with ConnectionIO
   */
  def findByPath(path: String): ConnectionIO[Content] = {
    sql"SELECT * FROM contents WHERE path = $path"
      .query[Content]
      .unique
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
