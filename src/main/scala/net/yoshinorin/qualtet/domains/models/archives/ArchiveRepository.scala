package net.yoshinorin.qualtet.domains.models.archives

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId

trait ArchiveRepository {

  /**
   * get all Articles
   *
   * @return Articles with ConnectionIO
   * TODO: order by
   */
  def get(contentTypeId: ContentTypeId): ConnectionIO[Seq[ResponseArchive]]
}
