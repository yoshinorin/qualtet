package net.yoshinorin.qualtet.domains.models.articles

import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieArticleRepository(doobie: DoobieContext) extends ArticleRepository {

  import doobie.ctx._

  def get(contentTypeId: String): ConnectionIO[Seq[ResponseArticle]] = {
    sql"""
      SELECT path, title, html_content, published_at, updated_at
      FROM contents
        WHERE content_type_id = $contentTypeId
        ORDER BY published_at desc
    """
      .query[ResponseArticle]
      .to[Seq]
  }

}