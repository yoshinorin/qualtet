package net.yoshinorin.qualtet

import cats.effect.{IO, ResourceApp}
import cats.effect.kernel.Resource
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import com.comcast.ip4s.*
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.http.{AuthProvider, CorsProvider}
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
  SystemRoute,
  TagRoute
}

import scala.concurrent.duration._

object BootStrap extends ResourceApp.Forever {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  logger.info(ApplicationInfo.asJson)

  val authProvider = new AuthProvider(Modules.authService)
  val corsProvider = new CorsProvider(Modules.config.cors)

  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val archiveRoute = new ArchiveRoute(Modules.archiveService)
  val articleRoute = new ArticleRoute(Modules.articleService)
  val authorRoute = new AuthorRoute(Modules.authorService)
  val authRoute = new AuthRoute(Modules.authService)
  val cacheRoute = new CacheRoute(authProvider, Modules.cacheService)
  val contentTypeRoute = new ContentTypeRoute(Modules.contentTypeService)
  val contentRoute = new ContentRoute(authProvider, Modules.contentService)
  val feedRoute = new FeedRoute(Modules.feedService)
  val homeRoute: HomeRoute = new HomeRoute()
  val searchRoute = new SearchRoute(Modules.searchService)
  val seriesRoute = new SeriesRoute(authProvider, Modules.seriesService)
  val sitemapRoute = new SitemapRoute(Modules.sitemapService)
  val systemRoute = new SystemRoute(Modules.config.http.endpoints.system)
  val tagRoute = new TagRoute(authProvider, Modules.tagService, Modules.articleService)

  val router = new net.yoshinorin.qualtet.http.Router(
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
    systemRoute,
    tagRoute
  )

  private[this] def server(host: Ipv4Address, port: Port, httpApp: HttpApp[IO]): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(host)
      .withPort(port)
      .withHttpApp(httpApp)
      .withLogger(org.typelevel.log4cats.slf4j.Slf4jLogger.getLoggerFromSlf4j(logger))
      .withShutdownTimeout(1.second)
      .build
  }

  override def run(args: List[String]): Resource[IO, Unit] = {
    Modules.migrator.migrate(Modules.contentTypeService)
    val host = Ipv4Address.fromString(Modules.config.http.host).getOrElse(ipv4"127.0.0.1")
    val port = Port.fromInt(Modules.config.http.port).getOrElse(port"9001")
    val httpApp: HttpApp[IO] = new HttpAppBuilder(router.withCors.orNotFound).build

    for {
      _ <- server(host, port, httpApp)
    } yield ()

  }
}
