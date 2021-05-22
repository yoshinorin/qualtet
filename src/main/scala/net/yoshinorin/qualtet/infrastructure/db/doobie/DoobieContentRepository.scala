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
    val q = quote(contents.insert(lift(data)))
    for {
      _ <- run(q).transact(doobie.transactor)
      c <- this.findByPath(data.path)
    } yield c
  }

  def findByPath(path: String): IO[Content] = {
    sql"SELECT * FROM contents WHERE path = $path"
      .query[Content]
      .unique
      .transact(doobie.transactor)
  }

  // TODO: should update
  def getAll: IO[Seq[Content]] = {
    sql"SELECT * FROM contents"
      .query[Content]
      .to[Seq]
      .transact(doobie.transactor)
  }
}
