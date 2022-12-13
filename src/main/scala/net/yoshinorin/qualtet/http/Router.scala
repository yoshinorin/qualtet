package net.yoshinorin.qualtet.http

import cats.effect._, cats.implicits._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server._
import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.syntax._

import net.yoshinorin.qualtet.http.routes.{ApiStatusRoute, ArchiveRoute, ArticleRoute, AuthRoute, AuthorRoute, CacheRoute, ContentRoute, HomeRoute}

// TODO: move somewhere
object PageQueryParam extends OptionalQueryParamDecoderMatcher[Int]("page")
object LimitQueryParam extends OptionalQueryParamDecoderMatcher[Int]("limit")

class Router(
  authorizationProvider: AuthorizationProvider,
  homeRoute: HomeRoute,
  apiStatusRoute: ApiStatusRoute,
  archiveRoute: ArchiveRoute,
  articleRoute: ArticleRoute,
  authorRoute: AuthorRoute,
  cacheRoute: CacheRoute,
  contentRoute: ContentRoute,
  authRoute: AuthRoute
) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def routes = Router(
    "/" -> home,
    "/archives" -> archives,
    "/articles" -> articles,
    "/authors" -> authors,
    "/caches" -> caches,
    "/contents" -> contents,
    "/status" -> status,
    "/token" -> token
  ).orNotFound

  def home: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => homeRoute.get
    case request @ _ =>
      logger.error(s"not implemented routes: ${request}")
      ???
    // TODO: return 404
  }

  def archives: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      archiveRoute.get
    case request @ _ =>
      logger.error(s"not implemented in authors routes: ${request}")
      ???
    // TODO: return 404
  }

  def articles: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root :? PageQueryParam(page) +& LimitQueryParam(limit) =>
      articleRoute.get(page, limit)
    case request @ _ =>
      logger.error(s"not implemented in authors routes: ${request}")
      ???
    // TODO: return 404
  }

  def authors: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      authorRoute.get
    case GET -> Root / authorName =>
      authorRoute.get(authorName)
    case request @ _ =>
      logger.error(s"not implemented in authors routes: ${request}")
      ???
    // TODO: return 404
  }

  def caches: HttpRoutes[IO] = authorizationProvider.authenticate(AuthedRoutes.of {
    case DELETE -> Root as author =>
      cacheRoute.delete(author._1)
    case request @ _ =>
      logger.error(s"not implemented in cache routes: ${request}")
      ???
    // TODO: return 404
  })

  // NOTE: must be compose `auth route` after `Non auth route`.
  def contents: HttpRoutes[IO] =
    contentWithoutAuth <+>
      authorizationProvider.authenticate(contentWithAuthed)

  private def contentWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] {
    // need slash on the prefix and suffix.
    // example: /yyyy/mm/dd/content-name/
    /* compile error
    case GET -> Root /: path =>
      contentRoute.get(path)
     */
    case request @ GET -> _ =>
      contentRoute.get(request.uri.path.toString().replace("/contents/", ""))
  }

  private def contentWithAuthed: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case request @ POST -> Root as payload =>
      contentRoute.post(payload)
    case DELETE -> Root / id as payload =>
      contentRoute.delete(id)
    case request @ _ =>
      logger.error(s"not implemented in contents routes with auth: ${request}")
      ???
    // TODO: return 404
  }

  def status: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => apiStatusRoute.get
    case request @ _ =>
      logger.error(s"not implemented routes: ${request}")
      ???
    // TODO: return 404
  }

  def token: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root =>
      authRoute.post(request)
    case request @ _ =>
      logger.error(s"not implemented in token routes: ${request}")
      ???
    // TODO: return 404
  }

  /*
  // caches routes
  val c: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case DELETE -> Root / "caches" as author =>
      cacheRoute.delete(author._1)
    case DELETE -> Root / "caches" / "" as author =>  // workaround for trailing slash
      cacheRoute.delete(author._1)
    case request @ _ =>
      logger.error(s"not implemented: ${request}")
      ???
  }

  val authedRoute: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case request @ POST -> Root / "contents" as payload =>
      contentRoute.post(payload)
    case DELETE -> "contents" /: id as payload =>
      contentRoute.delete(id)
    case request @ _ =>
      logger.error(s"not implemented: ${request}")
      ???
  }

  val nonAuthRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root / "token" =>
      authRoute.post(request)
    case request @ POST -> Root / "token" / "" =>  // workaround for trailing slash
      authRoute.post(request)
    case GET -> Root / "authors" =>
      authorRoute.get
    case GET -> Root / "authors" / authorName =>
      authorRoute.get(authorName)
    // need slash on the prefix and suffix.
    // example: /yyyy/mm/dd/content-name/
    case GET -> "contents" /: path =>
      contentRoute.get(path)
    case request @ _ =>
      logger.error(s"not implemented: ${request}")
      ???
  }
   */

}
