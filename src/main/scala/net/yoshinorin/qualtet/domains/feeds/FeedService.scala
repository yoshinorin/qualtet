package net.yoshinorin.qualtet.domains.feeds

import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import cats.effect.IO

class FeedService(articleService: ArticleService) {

  def get(queryParam: ArticlesQueryParameter): IO[Seq[ResponseFeed]] = {
    // TODO: Cache
    for {
      articles <- articleService.getWithCount(queryParam)
    } yield articles.articles.map(a => {
      new ResponseFeed(
        title = a.title,
        link = a.path,
        id = a.path,
        published = a.publishedAt,
        updated = a.updatedAt
      )
    })
  }

}
