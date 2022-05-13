package net.yoshinorin.qualtet.domains.archives

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.archives.RepositoryReqiests._

trait ArchiveRepository {

  /**
   * get all Articles
   *
   * @param GetByContentTypeId case class
   * @return Articles with ConnectionIO
   * TODO: order by
   */
  def dispatch(request: GetByContentTypeId): ConnectionIO[Seq[ResponseArchive]]
}
