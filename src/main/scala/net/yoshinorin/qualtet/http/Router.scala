package net.yoshinorin.qualtet.http

import cats.Monad
import org.http4s.server.{Router => Http4sRouter}
import net.yoshinorin.qualtet.http.routes.{
  ApiStatusRoute,
  ArchiveRoute,
  ArticleRoute,
  AuthRoute,
  AuthorRoute,
  CacheRoute,
  ContentRoute,
  ContentTypeRoute,
  FeedRoute,
  HomeRoute,
  SearchRoute,
  SeriesRoute,
  SitemapRoute,
  TagRoute
}

class Router[M[_]: Monad](
  corsProvider: CorsProvider,
  apiStatusRoute: ApiStatusRoute,
  archiveRoute: ArchiveRoute[M],
  articleRoute: ArticleRoute[M],
  authorRoute: AuthorRoute[M],
  authRoute: AuthRoute[M],
  cacheRoute: CacheRoute[M],
  contentRoute: ContentRoute[M],
  contentTypeRoute: ContentTypeRoute[M],
  feedRoute: FeedRoute[M],
  homeRoute: HomeRoute,
  searchRoute: SearchRoute[M],
  seriesRoute: SeriesRoute[M],
  sitemapRoute: SitemapRoute[M],
  tagRoute: TagRoute[M]
) {

  def withCors = corsProvider.httpRouter(routes)

  def routes = Http4sRouter(
    "/" -> homeRoute.index,
    "/archives" -> archiveRoute.index,
    "/articles" -> articleRoute.index,
    "/authors" -> authorRoute.index,
    "/caches" -> cacheRoute.index,
    "/contents" -> contentRoute.index,
    "/content-types" -> contentTypeRoute.index,
    "/feeds" -> feedRoute.index,
    "/search" -> searchRoute.index,
    "/series" -> seriesRoute.index,
    "/sitemaps" -> sitemapRoute.index,
    "/status" -> apiStatusRoute.index,
    "/tags" -> tagRoute.index,
    "/token" -> authRoute.index
  )
}
