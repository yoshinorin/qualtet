package net.yoshinorin.qualtet.http

import cats.effect._, cats.implicits._
import org.http4s.headers.Allow
import org.http4s.HttpRoutes
import org.http4s.server.{Router => Http4sRouter}
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
  SearchRoute,
  SitemapRoute
}
import net.yoshinorin.qualtet.http.routes.TagRoute
import net.yoshinorin.qualtet.syntax._

import scala.util.Try

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
  searchRoute: SearchRoute,
  sitemapRoute: SitemapRoute,
  tagRoute: TagRoute
) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private def methodNotAllowed(request: Request[IO], allow: Allow): IO[Response[IO]] = {
    logger.error(s"method not allowed: ${request}")
    MethodNotAllowed(allow)
  }

  // TODO: move somewhere & refactor
  private def queryParams(q: Map[String, String]): (Option[Int], Option[Int]) = {
    val a = Try(q.getOrElse("page", 1).toString.trim.toInt)
    val b = Try(q.getOrElse("limit", 1).toString.trim.toInt)

    (Some(a.getOrElse(1)), Some(b.getOrElse(10)))
  }

  def routes = Http4sRouter(
    "/" -> home,
    "/archives" -> archives,
    "/articles" -> articles,
    "/authors" -> authors,
    "/caches" -> caches,
    "/contents" -> contents,
    "/content-types" -> contentTypes,
    "/feeds" -> feeds,
    "/search" -> search,
    "/sitemaps" -> sitemaps,
    "/status" -> status,
    "/tags" -> tags,
    "/token" -> token
  ).orNotFound

  private[http] def home: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => homeRoute.get
    case request @ _ =>
      NotFound("Not found")
  }

  private[http] def archives: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      archiveRoute.get
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  private[http] def articles: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root =>
      val qp = queryParams(request.uri.query.params)
      articleRoute.get(qp._1, qp._2)
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  private[http] def authors: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      authorRoute.get
    case GET -> Root / authorName =>
      authorRoute.get(authorName)
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  private[http] def caches: HttpRoutes[IO] = authProvider.authenticate(AuthedRoutes.of {
    case DELETE -> Root as author =>
      cacheRoute.delete(author._1)
    case request @ _ =>
      methodNotAllowed(request.req, Allow(Set(DELETE)))
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
      methodNotAllowed(request.req, Allow(Set(GET, POST, DELETE)))
  }

  private[http] def contentTypes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => contentTypeRoute.get
    case GET -> Root / name => contentTypeRoute.get(name)
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  private[http] def feeds: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / name => feedRoute.get(name)
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  private[http] def search: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> _ =>
      searchRoute.search(request.uri.query.multiParams)
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  private[http] def sitemaps: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => sitemapRoute.get
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  private[http] def status: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => apiStatusRoute.get
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(GET)))
  }

  private[http] def token: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root =>
      authRoute.post(request)
    case request @ _ =>
      methodNotAllowed(request, Allow(Set(POST)))
  }

  private[http] def tags: HttpRoutes[IO] =
    tagsWithoutAuth <+>
      authProvider.authenticate(tagsWithAuthed)

  private[this] def tagsWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => tagRoute.get
    case request @ GET -> Root / nameOrId =>
      val qp = queryParams(request.uri.query.params)
      tagRoute.get(nameOrId, qp._1, qp._2)
  }

  private[this] def tagsWithAuthed: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case DELETE -> Root / nameOrId as payload =>
      tagRoute.delete(nameOrId)
    case request @ _ =>
      methodNotAllowed(request.req, Allow(Set(GET, DELETE)))
  }
}
