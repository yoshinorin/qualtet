package net.yoshinorin.qualtet.domains.search

trait SearchRepository[F[_]] {
  def search(query: List[String]): F[Seq[(Int, SearchResuletReadModel)]]
}

object SearchRepository {

  import doobie.Read
  import doobie.ConnectionIO
  import net.yoshinorin.qualtet.domains.contents.ContentPath

  given SearchRepository: SearchRepository[ConnectionIO] = {
    new SearchRepository[ConnectionIO] {

      given responseArticleWithCountRead: Read[(Int, SearchResuletReadModel)] =
        Read[(Int, (String, String, String, Long, Long))].map { case (cnt, (path, title, content, publishedAt, updatedAt)) =>
          (cnt, SearchResuletReadModel(ContentPath(path), title, content, publishedAt, updatedAt))
        }

      override def search(query: List[String]): ConnectionIO[Seq[(Int, SearchResuletReadModel)]] = {
        SearchQuery.search(query).to[Seq]
      }
    }

  }

}
