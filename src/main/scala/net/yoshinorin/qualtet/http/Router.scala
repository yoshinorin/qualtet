package net.yoshinorin.qualtet.http

import cats.Monad
import org.http4s.server.{Router => Http4sRouter}
import net.yoshinorin.qualtet.http.routes.HomeRoute
import net.yoshinorin.qualtet.http.routes.v1.{
  ArchiveRoute => ArchiveRouteV1,
  ArticleRoute => ArticleRouteV1,
  AuthRoute => AuthRouteV1,
  AuthorRoute => AuthorRouteV1,
  CacheRoute => CacheRouteV1,
  ContentRoute => ContentRouteV1,
  ContentTypeRoute => ContentTypeRouteV1,
  FeedRoute => FeedRouteV1,
  SearchRoute => SearchRouteV1,
  SeriesRoute => SeriesRouteV1,
  SitemapRoute => SitemapRouteV1,
  SystemRoute => SystemRouteV1,
  TagRoute => TagRouteV1
}

class Router[F[_]: Monad](
  corsProvider: CorsProvider,
  archiveRouteV1: ArchiveRouteV1[F],
  articleRouteV1: ArticleRouteV1[F],
  authorRouteV1: AuthorRouteV1[F],
  authRouteV1: AuthRouteV1[F],
  cacheRouteV1: CacheRouteV1[F],
  contentRouteV1: ContentRouteV1[F],
  contentTypeRouteV1: ContentTypeRouteV1[F],
  feedRouteV1: FeedRouteV1[F],
  homeRoute: HomeRoute,
  searchRouteV1: SearchRouteV1[F],
  seriesRouteV1: SeriesRouteV1[F],
  sitemapRouteV1: SitemapRouteV1[F],
  systemRouteV1: SystemRouteV1,
  tagRouteV1: TagRouteV1[F]
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
