package net.yoshinorin.qualtet.infrastructure.db.doobie

import doobie.implicits._
import cats.effect.IO
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentRepository}

class DoobieContentRepository(doobie: DoobieContext) extends ContentRepository {

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return created Content
   */
  override def insert(data: Content): Content = ???

  // TODO: should update
  def getAll: IO[Seq[Content]] = {
    sql"SELECT * FROM contents"
      .query[Content]
      .to[Seq]
      .transact(doobie.transactor)
  }
}
