package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

trait ArchiveRepository[F[_]] {
  def get(contentTypeId: ContentTypeId): F[Seq[ArchiveReadModel]]
}

object ArchiveRepository {

  import doobie.Read
  import doobie.ConnectionIO
  import net.yoshinorin.qualtet.domains.contents.ContentPath

  given ArchiveRepository: ArchiveRepository[ConnectionIO] = {
    new ArchiveRepository[ConnectionIO] {
      given archivesRead: Read[ArchiveReadModel] =
        Read[(String, String, Long)].map { case (path, title, publishedAt) =>
          ArchiveReadModel(ContentPath.unsafe(path), title, publishedAt)
        }

      override def get(contentTypeId: ContentTypeId): ConnectionIO[Seq[ArchiveReadModel]] = ArchiveQuery.get(contentTypeId).to[Seq]
    }
  }

}
