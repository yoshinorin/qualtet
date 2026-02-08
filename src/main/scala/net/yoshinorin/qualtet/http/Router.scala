package net.yoshinorin.qualtet.http

import cats.Monad
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

class Router[G[_]: Monad @nowarn](
  corsProvider: CorsProvider,
  archiveRouteV1: ArchiveRouteV1[G],
  articleRouteV1: ArticleRouteV1[G],
  authorRouteV1: AuthorRouteV1[G],
  authRouteV1: AuthRouteV1[G],
  cacheRouteV1: CacheRouteV1[G],
  contentRouteV1: ContentRouteV1[G],
  contentTypeRouteV1: ContentTypeRouteV1[G],
  feedRouteV1: FeedRouteV1[G],
  homeRoute: HomeRoute,
  searchRouteV1: SearchRouteV1[G],
  seriesRouteV1: SeriesRouteV1[G],
  sitemapRouteV1: SitemapRouteV1[G],
  systemRouteV1: SystemRouteV1,
  tagRouteV1: TagRouteV1[G]
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
