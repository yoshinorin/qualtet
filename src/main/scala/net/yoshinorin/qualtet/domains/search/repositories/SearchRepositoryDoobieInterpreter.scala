package net.yoshinorin.qualtet.domains.search

import doobie.Read
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contents.Path

class SearchRepositoryDoobieInterpreter extends SearchRepository[ConnectionIO] {

  given responseArticleWithCountRead: Read[(Int, ResponseSearch)] =
    Read[(Int, (String, String, String, Long, Long))].map { case (cnt, (path, title, content, publishedAt, updatedAt)) =>
      (cnt, ResponseSearch(Path(path), title, content, publishedAt, updatedAt))
    }

  override def search(query: List[String]): ConnectionIO[Seq[(Int, ResponseSearch)]] = {
    SearchQuery.search(query).to[Seq]
  }

}
