package net.yoshinorin.qualtet

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO
import org.http4s._
import org.http4s.server.middleware.Logger
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.http.AuthProvider
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
  SearchRoute,
  SitemapRoute,
  TagRoute
}
import net.yoshinorin.qualtet.http.routes.CacheRoute
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
// import scala.io.StdIn

@SuppressWarnings(Array("org.wartremover.warts.ScalaApp")) // Not yet migrate to Scala3
object BootStrap extends IOApp {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  logger.info("booting...")

  Modules.migrator.migrate(Modules.contentTypeService)

  val authProvider = new AuthProvider(Modules.authService)

  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val archiveRoute = new ArchiveRoute(Modules.archiveService)
  val articleRoute = new ArticleRoute(Modules.articleService)
  val authorRoute = new AuthorRoute(Modules.authorService)
  val authRoute = new AuthRoute(Modules.authService)
  val cacheRoute = new CacheRoute(Modules.cacheService)
  val contentTypeRoute = new ContentTypeRoute(Modules.contentTypeService)
  val contentRoute = new ContentRoute(Modules.contentService)
  val feedRoute = new FeedRoute(Modules.feedService)
  val homeRoute: HomeRoute = new HomeRoute()
  val searchRoute = new SearchRoute(Modules.searchService)
  val sitemapRoute = new SitemapRoute(Modules.sitemapService)
  val tagRoute = new TagRoute(Modules.tagService, Modules.articleService)

  logger.info("created all instances")

  val router = new net.yoshinorin.qualtet.http.Router(
    authProvider,
    apiStatusRoute,
    archiveRoute,
    articleRoute,
    authorRoute,
    authRoute,
    cacheRoute,
    contentRoute,
    contentTypeRoute,
    feedRoute,
    homeRoute,
    searchRoute,
    sitemapRoute,
    tagRoute
  )

  def run(args: List[String]): IO[ExitCode] = {

    // TODO: filter & format log
    val httpAppWithLogger: HttpApp[IO] = Logger.httpApp(true, false)(router.routes)

    logger.info("starting http server...")
    EmberServerBuilder
      .default[IO]
      .withHost(Ipv4Address.fromString(Modules.config.http.host).get)
      .withPort(Port.fromInt(Modules.config.http.port).get)
      .withHttpApp(httpAppWithLogger)
      .withLogger(org.typelevel.log4cats.slf4j.Slf4jLogger.getLoggerFromSlf4j(logger))
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
