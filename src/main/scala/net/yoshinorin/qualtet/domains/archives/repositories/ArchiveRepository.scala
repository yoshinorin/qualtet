package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.period.Period

trait ArchiveRepository[F[_]] {
  def get(contentTypeId: ContentTypeId, period: Seq[Period]): F[Seq[ArchiveReadModel]]
}

object ArchiveRepository {

  import net.yoshinorin.qualtet.domains.contents.ContentPath

  import org.typelevel.doobie.{ConnectionIO, Read}

  given ArchiveRepository: ArchiveRepository[ConnectionIO] = {
    new ArchiveRepository[ConnectionIO] {
      given archivesRead: Read[ArchiveReadModel] =
        Read[(String, String, Long)].map { case (path, title, publishedAt) =>
          ArchiveReadModel(ContentPath.fromTrusted(path), title, publishedAt)
        }

      override def get(contentTypeId: ContentTypeId, periods: Seq[Period]): ConnectionIO[Seq[ArchiveReadModel]] =
        ArchiveQuery.get(contentTypeId, periods).to[Seq]
    }
  }

}
