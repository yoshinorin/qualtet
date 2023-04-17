package net.yoshinorin.qualtet.domains.archives

import doobie.Read
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.contents.Path

class ArchiveRepositoryDoobieInterpreter extends ArchiveRepository[ConnectionIO] {

  given responseArchivesRead: Read[ResponseArchive] =
    Read[(String, String, Long)].map { case (path, title, publishedAt) =>
      ResponseArchive(Path(path), title, publishedAt)
    }

  override def get(contentTypeId: ContentTypeId): ConnectionIO[Seq[ResponseArchive]] = ArchiveQuery.get(contentTypeId).to[Seq]
}
