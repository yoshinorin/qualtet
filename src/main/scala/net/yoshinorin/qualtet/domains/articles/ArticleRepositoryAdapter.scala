package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.pagination.Pagination
import net.yoshinorin.qualtet.domains.series.{SeriesName, SeriesPath}
import net.yoshinorin.qualtet.domains.tags.{TagName, TagPath}

import cats.Monad
import cats.data.ContT
import cats.implicits.*
import scala.annotation.nowarn

class ArticleRepositoryAdapter[F[_]: Monad](
  articleRepository: ArticleRepository[F]
) {

  private[domains] def getWithCount(
    contentTypeId: ContentTypeId,
    @nowarn none: Unit = (),
    pagination: Pagination
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { _ =>
      articleRepository.getWithCount(contentTypeId, pagination).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

  private[domains] def findByTagNameWithCount(
    contentTypeId: ContentTypeId,
    tagName: TagName,
    @nowarn pagination: Pagination
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { _ =>
      articleRepository.findByTagNameWithCount(contentTypeId, tagName, pagination).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

  private[domains] def findByTagPathWithCount(
    contentTypeId: ContentTypeId,
    tagPath: TagPath,
    @nowarn pagination: Pagination
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { _ =>
      articleRepository.findByTagPathWithCount(contentTypeId, tagPath, pagination).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

  private[domains] def findBySeriesNameWithCount(
    contentTypeId: ContentTypeId,
    seriesName: SeriesName,
    @nowarn pagination: Pagination // TODO: `Optional`
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { _ =>
      articleRepository.findBySeriesNameWithCount(contentTypeId, seriesName).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

  private[domains] def findBySeriesPathWithCount(
    contentTypeId: ContentTypeId,
    seriesPath: SeriesPath,
    @nowarn pagination: Pagination // TODO: `Optional`
  ): ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] = {
    ContT.apply[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]] { _ =>
      articleRepository.findBySeriesPathWithCount(contentTypeId, seriesPath).map { article =>
        article.map { case (count, article) =>
          (count, ArticleResponseModel(article.id, article.path, article.title, article.content, article.publishedAt, article.updatedAt))
        }
      }
    }
  }

}
