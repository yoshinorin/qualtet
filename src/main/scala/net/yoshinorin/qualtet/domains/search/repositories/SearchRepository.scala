package net.yoshinorin.qualtet.domains.search

trait SearchRepository[F[_]] {
  def search(query: List[String]): F[Seq[(Int, SearchResuletReadModel)]]
}

object SearchRepository {

  import net.yoshinorin.qualtet.domains.contents.ContentPath

  import org.typelevel.doobie.{ConnectionIO, Get, Read}

  given SearchRepository: SearchRepository[ConnectionIO] = {
    new SearchRepository[ConnectionIO] {

      given contentPathGet: Get[ContentPath] = Get[String].map(ContentPath.fromTrusted)

      given responseArticleWithCountRead: Read[(Int, SearchResuletReadModel)] =
        Read[(Int, (ContentPath, String, String, Long, Long))].map { case (cnt, (path, title, content, publishedAt, updatedAt)) =>
          (
            cnt,
            SearchResuletReadModel(
              path,
              title,
              content,
              publishedAt,
              updatedAt
            )
          )
        }

      override def search(query: List[String]): ConnectionIO[Seq[(Int, SearchResuletReadModel)]] = {
        SearchQuery.search(query).to[Seq]
      }
    }

  }

}
