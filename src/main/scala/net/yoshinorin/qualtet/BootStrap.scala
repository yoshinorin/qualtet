package net.yoshinorin.qualtet

import cats._
import cats.implicits._
import cats.effect.IOApp
import cats.effect.ExitCode
import cats.data.Kleisli
import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.middleware.Logger
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.http.AuthorizationProvider
import net.yoshinorin.qualtet.http.routes.{
  ApiStatusRoute,
  ArchiveRoute,
  ArticleRoute,
  AuthRoute,
  AuthorRoute,
  ContentRoute,
  ContentTypeRoute,
  FeedRoute,
  HomeRoute,
  SitemapRoute,
  TagRoute
}
import net.yoshinorin.qualtet.infrastructure.db.Migration
import net.yoshinorin.qualtet.http.routes.CacheRoute
import org.http4s.server.Router
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
// import scala.io.StdIn

@SuppressWarnings(Array("org.wartremover.warts.ScalaApp")) // Not yet migrate to Scala3
object BootStrap extends IOApp {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  logger.info("booting...")

  /*
  val tagRoute: TagRoute = new TagRoute(Modules.authService, Modules.tagService, Modules.articleService)
  val contentTypeRoute: ContentTypeRoute = new ContentTypeRoute(Modules.contentTypeService)
  val sitemapRoute: SitemapRoute = new SitemapRoute(Modules.sitemapService)
  val feedRoute: FeedRoute = new FeedRoute(Modules.feedService)
   */

  logger.info("created all instances")

  Migration.migrate(Modules.contentTypeService)

  val authorizationProvider: AuthorizationProvider = new AuthorizationProvider(Modules.authService)

  val homeRoute: HomeRoute = new HomeRoute()
  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val archiveRoute: ArchiveRoute = new ArchiveRoute(Modules.archiveService)
  val articleRoute: ArticleRoute = new ArticleRoute(Modules.articleService)
  val authorRoute: AuthorRoute = new AuthorRoute(Modules.authorService)
  val cacheRoute: CacheRoute = new CacheRoute(authorizationProvider, Modules.cacheService)
  val contentRoute: ContentRoute = new ContentRoute(Modules.contentService)
  val authRoute: AuthRoute = new AuthRoute(Modules.authService)

  val router = new net.yoshinorin.qualtet.http.Router(
    authorizationProvider,
    homeRoute,
    apiStatusRoute,
    archiveRoute,
    articleRoute,
    authorRoute,
    cacheRoute,
    contentRoute,
    authRoute
  )

  val httpRoutes = router.routes

  def run(args: List[String]): IO[ExitCode] = {

    // TODO: filter & format log
    val httpAppWithLogger = Logger.httpApp(true, false)(httpRoutes)

    logger.info("starting http server...")
    EmberServerBuilder
      .default[IO]
      .withHost(Ipv4Address.fromString(Config.httpHost).get)
      .withPort(Port.fromInt(Config.httpPort).get)
      .withHttpApp(httpAppWithLogger)
      .withLogger(org.typelevel.log4cats.slf4j.Slf4jLogger.getLoggerFromSlf4j(logger))
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
