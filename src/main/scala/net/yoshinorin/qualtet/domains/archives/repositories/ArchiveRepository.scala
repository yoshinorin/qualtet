package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

trait ArchiveRepository[F[_]] {
  def get(contentTypeId: ContentTypeId): F[Seq[ResponseArchive]]
}

object ArchiveRepository {

  import doobie.Read
  import doobie.ConnectionIO
  import net.yoshinorin.qualtet.domains.contents.Path

  given ArchiveRepository: ArchiveRepository[ConnectionIO] = {
    new ArchiveRepository[ConnectionIO] {
      given responseArchivesRead: Read[ResponseArchive] =
        Read[(String, String, Long)].map { case (path, title, publishedAt) =>
          ResponseArchive(Path(path), title, publishedAt)
        }

      override def get(contentTypeId: ContentTypeId): ConnectionIO[Seq[ResponseArchive]] = ArchiveQuery.get(contentTypeId).to[Seq]
    }
  }

}
