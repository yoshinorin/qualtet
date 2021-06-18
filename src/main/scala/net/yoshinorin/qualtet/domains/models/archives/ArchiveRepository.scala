package net.yoshinorin.qualtet.domains.models.archives

import doobie.ConnectionIO

trait ArchiveRepository {

  /**
   * get all Articles
   *
   * @return Articles with ConnectionIO
   * TODO: order by
   */
  def get(contentTypeId: String): ConnectionIO[Seq[ResponseArchive]]
}
