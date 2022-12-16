package net.yoshinorin.qualtet.http

import cats.effect._, cats.implicits._
import org.http4s.server._
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.dsl.io._
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
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
  SitemapRoute
}
import net.yoshinorin.qualtet.http.routes.TagRoute
import net.yoshinorin.qualtet.syntax._

// TODO: move somewhere
object PageQueryParam extends OptionalQueryParamDecoderMatcher[Int]("page")
object LimitQueryParam extends OptionalQueryParamDecoderMatcher[Int]("limit")

class Router(
  authProvider: AuthProvider,
  apiStatusRoute: ApiStatusRoute,
  archiveRoute: ArchiveRoute,
  articleRoute: ArticleRoute,
  authorRoute: AuthorRoute,
  authRoute: AuthRoute,
  cacheRoute: CacheRoute,
  contentRoute: ContentRoute,
  contentTypeRoute: ContentTypeRoute,
  feedRoute: FeedRoute,
  homeRoute: HomeRoute,
  sitemapRoute: SitemapRoute,
  tagRoute: TagRoute
) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def routes = Router(
    "/" -> home,
    "/archives" -> archives,
    "/articles" -> articles,
    "/authors" -> authors,
    "/caches" -> caches,
    "/contents" -> contents,
    "/content-types" -> contentTypes,
    "/feeds" -> feeds,
    "/sitemaps" -> sitemaps,
    "/status" -> status,
    "/tags" -> tags,
    "/token" -> token
  ).orNotFound

  private[http] def home: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => homeRoute.get
    case request @ _ =>
      logger.error(s"not implemented routes: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def archives: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      archiveRoute.get
    case request @ _ =>
      logger.error(s"not implemented in authors routes: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def articles: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root :? PageQueryParam(page) +& LimitQueryParam(limit) =>
      articleRoute.get(page, limit)
    case request @ _ =>
      logger.error(s"not implemented in authors routes: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def authors: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      authorRoute.get
    case GET -> Root / authorName =>
      authorRoute.get(authorName)
    case request @ _ =>
      logger.error(s"not implemented in authors routes: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def caches: HttpRoutes[IO] = authProvider.authenticate(AuthedRoutes.of {
    case DELETE -> Root as author =>
      cacheRoute.delete(author._1)
    case request @ _ =>
      logger.error(s"not implemented in cache routes: ${request}")
      ???
    // TODO: return 404
  })

  // NOTE: must be compose `auth route` after `Non auth route`.
  private[http] def contents: HttpRoutes[IO] =
    contentWithoutAuth <+>
      authProvider.authenticate(contentWithAuthed)

  private[this] def contentWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] {
    // need slash on the prefix and suffix.
    // example: /yyyy/mm/dd/content-name/
    /* compile error
    case GET -> Root /: path =>
      contentRoute.get(path)
     */
    case request @ GET -> _ =>
      contentRoute.get(request.uri.path.toString().replace("/contents/", ""))
  }

  private[this] def contentWithAuthed: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case request @ POST -> Root as payload =>
      contentRoute.post(payload)
    case DELETE -> Root / id as payload =>
      contentRoute.delete(id)
    case request @ _ =>
      logger.error(s"not implemented in contents routes with auth: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def contentTypes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => contentTypeRoute.get
    case GET -> Root / name => contentTypeRoute.get(name)
    case request @ _ =>
      logger.error(s"not implemented in contentTypes routes: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def feeds: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / name => feedRoute.get(name)
    case request @ _ =>
      logger.error(s"not implemented in feed routes: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def sitemaps: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => sitemapRoute.get
    case request @ _ =>
      logger.error(s"not implemented in sitemaps routes: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def status: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => apiStatusRoute.get
    case request @ _ =>
      logger.error(s"not implemented in status routes: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def token: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root =>
      authRoute.post(request)
    case request @ _ =>
      logger.error(s"not implemented in token routes: ${request}")
      ???
    // TODO: return 404
  }

  private[http] def tags: HttpRoutes[IO] =
    tagsWithoutAuth <+>
      authProvider.authenticate(tagsWithAuthed)

  private[this] def tagsWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => tagRoute.get
    case GET -> Root / nameOrId :? PageQueryParam(page) +& LimitQueryParam(limit) => tagRoute.get(nameOrId, page, limit)
    case request @ _ =>
      logger.error(s"not implemented in tags routes: ${request}")
      ???
    // TODO: return 404
  }

  private[this] def tagsWithAuthed: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case DELETE -> Root / nameOrId as payload =>
      tagRoute.delete(nameOrId)
    case request @ _ =>
      logger.error(s"not implemented in tags routes with auth: ${request}")
      ???
    // TODO: return 404
  }
}
