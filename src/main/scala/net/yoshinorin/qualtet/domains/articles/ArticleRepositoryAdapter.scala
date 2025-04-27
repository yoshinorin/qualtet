package net.yoshinorin.qualtet.domains.articles

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.{TagName, TagPath}
import net.yoshinorin.qualtet.domains.series.SeriesPath
import net.yoshinorin.qualtet.domains.Pagination

class ArticleRepositoryAdapter[F[_]: Monad](
  articleRepository: ArticleRepository[F]
) {

  private[domains] def getWithCount(
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

  private[domains] def findByTagNameWithCount(
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

  private[domains] def findByTagPathWithCount(
    contentTypeId: ContentTypeId,
    tagPath: TagPath,
    queryParams: Pagination
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { next =>
      articleRepository.findByTagPathWithCount(contentTypeId, tagPath, queryParams).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

  private[domains] def findBySeriesPathWithCount(
    contentTypeId: ContentTypeId,
    seriesPath: SeriesPath,
    queryParams: Pagination // TODO: `Optional`
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { next =>
      articleRepository.findBySeriesPathWithCount(contentTypeId, seriesPath).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

}
