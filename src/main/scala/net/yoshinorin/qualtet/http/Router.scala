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

// TODO: delete duplicated routes.
// e.g) `POST -> Root / "token"` and `request @ POST -> Root / "token" / ""`
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

  // NOTE: must be compose `auth route` after `Non auth route`.
  /*
  def route: HttpRoutes[IO] = nonAuthRoute <+>
    cacheRouter <+>
    authorizationProvider.authenticate(authedRoute)
   */

  def route: HttpRoutes[IO] = server.Router(
    "/" -> HttpRoutes.of {
      case GET -> Root => homeRoute.get
      case request @ _ =>
        logger.error(s"not implemented routes: ${request}")
        ???
      // TODO: return 404
    }
  )

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

  def caches = authorizationProvider.authenticate(AuthedRoutes.of {
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

  val contentWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] {
    // need slash on the prefix and suffix.
    // example: /yyyy/mm/dd/content-name/
    /* compile error
    case GET -> Root /: path =>
      contentRoute.get(path)
     */
    case request @ GET -> _ =>
      contentRoute.get(request.uri.path.toString().replace("/contents/", ""))
  }

  val contentWithAuthed: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case request @ POST -> Root as payload =>
      contentRoute.post(payload)
    case DELETE -> Root / id as payload =>
      contentRoute.delete(id)
    case request @ _ =>
      logger.error(s"not implemented in contents routes with auth: ${request}")
      ???
    // TODO: return 404
  }

  def token = HttpRoutes.of[IO] {
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
