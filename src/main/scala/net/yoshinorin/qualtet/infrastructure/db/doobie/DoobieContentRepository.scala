package net.yoshinorin.qualtet.infrastructure.db.doobie

import cats.effect.IO
import doobie.implicits._
import io.getquill.{idiom => _, _}
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentRepository}

class DoobieContentRepository(doobie: DoobieContext) extends ContentRepository {

  import doobie.ctx._

  private val contents = quote(querySchema[Content]("contents"))

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return created Content
   */
  def insert(data: Content): IO[Content] = {
    //val q = quote(contents.insert(lift(data)))
    //val q = quote { query[Content].insert(lift(data)) }
    ???
  }

  // TODO: should update
  def getAll: IO[Seq[Content]] = {
    sql"SELECT * FROM contents"
      .query[Content]
      .to[Seq]
      .transact(doobie.transactor)
  }
}
