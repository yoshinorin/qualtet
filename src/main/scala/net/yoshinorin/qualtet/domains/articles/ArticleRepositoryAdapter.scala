package net.yoshinorin.qualtet.domains.articles

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.domains.Pagination

class ArticleRepositoryAdapter[F[_]: Monad](
  articleRepository: ArticleRepository[F]
) {

  def getWithCount(
    contentTypeId: ContentTypeId,
    none: Unit = (),
    queryParams: Pagination
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { next =>
      articleRepository.getWithCount(contentTypeId, queryParams).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

  def findByTagNameWithCount(
    contentTypeId: ContentTypeId,
    tagName: TagName,
    queryParams: Pagination
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { next =>
      articleRepository.findByTagNameWithCount(contentTypeId, tagName, queryParams).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

  def findBySeriesNameWithCount(
    contentTypeId: ContentTypeId,
    seriesName: SeriesName,
    queryParams: Pagination // TODO: `Optional`
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { next =>
      articleRepository.findBySeriesNameWithCount(contentTypeId, seriesName).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

}
