package net.yoshinorin.qualtet

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO
import org.http4s.*
import org.http4s.server.middleware.Logger
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{RequestId, ResponseTiming}
import com.comcast.ip4s.*
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.http.CorsProvider
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
  SeriesRoute,
  SitemapRoute,
  TagRoute
}
import net.yoshinorin.qualtet.http.routes.CacheRoute

// import scala.io.StdIn

object BootStrap extends IOApp {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  val authProvider = new AuthProvider(Modules.authService)
  val corsProvider = new CorsProvider(Modules.config.cors)

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
  val seriesRoute = new SeriesRoute(Modules.seriesService)
  val sitemapRoute = new SitemapRoute(Modules.sitemapService)
  val tagRoute = new TagRoute(Modules.tagService, Modules.articleService)

  val router = new net.yoshinorin.qualtet.http.Router(
    authProvider,
    corsProvider,
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
    seriesRoute,
    sitemapRoute,
    tagRoute
  )

  def run(args: List[String]): IO[ExitCode] = {

    Modules.migrator.migrate(Modules.contentTypeService)

    def buildHttpApp: HttpApp[IO] = {
      // NOTE: https://github.com/http4s/http4s/blob/v1.0.0-M40/server/shared/src/main/scala/org/http4s/server/middleware/RequestId.scala
      val withRequestIdHeader = RequestId(router.withCors.orNotFound)

      // NOTE: https://github.com/http4s/http4s/blob/v1.0.0-M40/server/shared/src/main/scala/org/http4s/server/middleware/ResponseTiming.scala
      val withResponseTimingHeader = ResponseTiming(withRequestIdHeader)
      // TODO: filter & format log
      Logger.httpApp(logHeaders = true, logBody = false)(withResponseTimingHeader)
    }

    val host = Ipv4Address.fromString(Modules.config.http.host).getOrElse(ipv4"127.0.0.1")
    val port = Port.fromInt(Modules.config.http.port).getOrElse(port"9001")
    val httpApp: HttpApp[IO] = buildHttpApp

    logger.info("starting http server...")
    EmberServerBuilder
      .default[IO]
      .withHost(host)
      .withPort(port)
      .withHttpApp(httpApp)
      .withLogger(org.typelevel.log4cats.slf4j.Slf4jLogger.getLoggerFromSlf4j(logger))
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
