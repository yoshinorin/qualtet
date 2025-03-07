package net.yoshinorin.qualtet.domains.articles

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.errors.{ArticleNotFound, ContentTypeNotFound}
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.domains.{ArticlesPagination, Limit, Order, Page, Pagination, PaginationOps, PaginationRequestModel, TagsPagination}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class ArticleService[F[_]: Monad](
  articleRepository: ArticleRepository[F],
  articlesPagination: PaginationOps[ArticlesPagination],
  tagsPagination: PaginationOps[TagsPagination],
  contentTypeService: ContentTypeService[F]
)(using executer: Executer[F, IO]) {

  def cont(
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

  def tagCont(
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

  def seriesCont(
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

  def get[A](
    data: A = (),
    queryParam: Pagination
  )(
    f: (ContentTypeId, A, Pagination) => ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]]
  ): IO[ArticleWithCountResponseModel] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(ContentTypeNotFound(detail = "content-type not found: article"))
      articlesWithCount <- executer.transact(f(c.id, data, queryParam))
    } yield
      if (articlesWithCount.nonEmpty) {
        ArticleWithCountResponseModel(articlesWithCount.map(_._1).headOption.getOrElse(0), articlesWithCount.map(_._2))
      } else {
        throw ArticleNotFound(detail = "articles not found")
      }
  }

  def getWithCount(p: PaginationRequestModel): IO[ArticleWithCountResponseModel] = {
    this.get(queryParam = articlesPagination.make(p))(cont)
  }

  def getWithCount(p: Pagination): IO[ArticleWithCountResponseModel] = {
    this.get(queryParam = p)(cont)
  }

  def getByTagNameWithCount(tagName: TagName, p: PaginationRequestModel): IO[ArticleWithCountResponseModel] = {
    this.get(tagName, tagsPagination.make(p))(tagCont)
  }

  def getBySeriesName(seriesName: SeriesName): IO[ArticleWithCountResponseModel] = {
    this.get(seriesName, articlesPagination.make(Page(0), Limit(100), Order.DESC))(seriesCont)
  }

}
