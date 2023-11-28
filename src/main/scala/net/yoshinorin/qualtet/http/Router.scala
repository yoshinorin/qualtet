package net.yoshinorin.qualtet.http

import cats.Monad
import org.http4s.server.{Router => Http4sRouter}
import net.yoshinorin.qualtet.http.routes.{
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
  SystemRoute,
  TagRoute
}

class Router[F[_]: Monad](
  corsProvider: CorsProvider,
  archiveRoute: ArchiveRoute[F],
  articleRoute: ArticleRoute[F],
  authorRoute: AuthorRoute[F],
  authRoute: AuthRoute[F],
  cacheRoute: CacheRoute[F],
  contentRoute: ContentRoute[F],
  contentTypeRoute: ContentTypeRoute[F],
  feedRoute: FeedRoute[F],
  homeRoute: HomeRoute,
  searchRoute: SearchRoute[F],
  seriesRoute: SeriesRoute[F],
  sitemapRoute: SitemapRoute[F],
  systemRoute: SystemRoute,
  tagRoute: TagRoute[F]
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
    "/system" -> systemRoute.index,
    "/tags" -> tagRoute.index,
    "/token" -> authRoute.index
  )
}
