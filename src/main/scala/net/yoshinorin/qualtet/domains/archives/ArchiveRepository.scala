package net.yoshinorin.qualtet.domains.archives

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

trait ArchiveRepository {

  /**
   * get all Articles
   *
   * @return Articles with ConnectionIO
   * TODO: order by
   */
  def get(contentTypeId: ContentTypeId): ConnectionIO[Seq[ResponseArchive]]
}
