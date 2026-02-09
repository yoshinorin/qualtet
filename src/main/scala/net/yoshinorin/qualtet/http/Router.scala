package net.yoshinorin.qualtet.http

import cats.Monad
import cats.effect.Concurrent
import org.http4s.server.Router as Http4sRouter
import net.yoshinorin.qualtet.http.routes.HomeRoute
import net.yoshinorin.qualtet.http.routes.v1.{
  ArchiveRoute as ArchiveRouteV1,
  ArticleRoute as ArticleRouteV1,
  AuthRoute as AuthRouteV1,
  AuthorRoute as AuthorRouteV1,
  CacheRoute as CacheRouteV1,
  ContentRoute as ContentRouteV1,
  ContentTypeRoute as ContentTypeRouteV1,
  FeedRoute as FeedRouteV1,
  SearchRoute as SearchRouteV1,
  SeriesRoute as SeriesRouteV1,
  SitemapRoute as SitemapRouteV1,
  SystemRoute as SystemRouteV1,
  TagRoute as TagRouteV1
}

import scala.annotation.nowarn

class Router[F[_]: Concurrent, G[_]: Monad @nowarn](
  corsProvider: CorsProvider[F],
  archiveRouteV1: ArchiveRouteV1[F, G],
  articleRouteV1: ArticleRouteV1[F, G],
  authorRouteV1: AuthorRouteV1[F, G],
  authRouteV1: AuthRouteV1[F, G],
  cacheRouteV1: CacheRouteV1[F, G],
  contentRouteV1: ContentRouteV1[F, G],
  contentTypeRouteV1: ContentTypeRouteV1[F, G],
  feedRouteV1: FeedRouteV1[F, G],
  homeRoute: HomeRoute[F],
  searchRouteV1: SearchRouteV1[F, G],
  seriesRouteV1: SeriesRouteV1[F, G],
  sitemapRouteV1: SitemapRouteV1[F, G],
  systemRouteV1: SystemRouteV1[F],
  tagRouteV1: TagRouteV1[F, G]
) {

  def withCors = corsProvider.httpRouter(routes)

  def routes = Http4sRouter(
    "/" -> homeRoute.index,
    "/v1/archives" -> archiveRouteV1.index,
    "/v1/articles" -> articleRouteV1.index,
    "/v1/authors" -> authorRouteV1.index,
    "/v1/caches" -> cacheRouteV1.index,
    "/v1/contents" -> contentRouteV1.index,
    "/v1/content-types" -> contentTypeRouteV1.index,
    "/v1/feeds" -> feedRouteV1.index,
    "/v1/search" -> searchRouteV1.index,
    "/v1/series" -> seriesRouteV1.index,
    "/v1/sitemaps" -> sitemapRouteV1.index,
    "/v1/system" -> systemRouteV1.index,
    "/v1/tags" -> tagRouteV1.index,
    "/v1/token" -> authRouteV1.index
  )
}
