package net.yoshinorin.qualtet

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.{LoggerFactory => Log4CatsLoggerFactory}
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import com.comcast.ip4s.*
import net.yoshinorin.qualtet.http.{AuthProvider, CorsProvider}
import net.yoshinorin.qualtet.http.routes.HomeRoute
import net.yoshinorin.qualtet.http.routes.v1.{
  ArchiveRoute => ArchiveRouteV1,
  ArticleRoute => ArticleRouteV1,
  AuthRoute => AuthRouteV1,
  AuthorRoute => AuthorRouteV1,
  CacheRoute => CacheRouteV1,
  ContentRoute => ContentRouteV1,
  ContentTypeRoute => ContentTypeRouteV1,
  FeedRoute => FeedRouteV1,
  SearchRoute => SearchRouteV1,
  SeriesRoute => SeriesRouteV1,
  SitemapRoute => SitemapRouteV1,
  SystemRoute => SystemRouteV1,
  TagRoute => TagRouteV1
}

import scala.concurrent.duration._

object BootStrap extends IOApp {

  import net.yoshinorin.qualtet.Modules.log4catsLogger

  val logger: SelfAwareStructuredLogger[IO] = Log4CatsLoggerFactory[IO].getLoggerFromClass(this.getClass)

  val authProvider = new AuthProvider(Modules.authService)
  val corsProvider = new CorsProvider(Modules.config.cors)

  val archiveRouteV1 = new ArchiveRouteV1(Modules.archiveService)
  val articleRouteV1 = new ArticleRouteV1(Modules.articleService)
  val authorRouteV1 = new AuthorRouteV1(Modules.authorService)
  val authRouteV1 = new AuthRouteV1(Modules.authService)
  val cacheRouteV1 = new CacheRouteV1(authProvider, Modules.cacheService)
  val contentTypeRouteV1 = new ContentTypeRouteV1(Modules.contentTypeService)
  val contentRouteV1 = new ContentRouteV1(authProvider, Modules.contentService)
  val feedRouteV1 = new FeedRouteV1(Modules.feedService)
  val homeRoute: HomeRoute = new HomeRoute()
  val searchRouteV1 = new SearchRouteV1(Modules.searchService)
  val seriesRouteV1 = new SeriesRouteV1(authProvider, Modules.seriesService)
  val sitemapRouteV1 = new SitemapRouteV1(Modules.sitemapService)
  val systemRouteV1 = new SystemRouteV1(Modules.config.http.endpoints.system)
  val tagRouteV1 = new TagRouteV1(authProvider, Modules.tagService, Modules.articleService)

  val router = new net.yoshinorin.qualtet.http.Router(
    corsProvider,
    archiveRouteV1,
    articleRouteV1,
    authorRouteV1,
    authRouteV1,
    cacheRouteV1,
    contentRouteV1,
    contentTypeRouteV1,
    feedRouteV1,
    homeRoute,
    searchRouteV1,
    seriesRouteV1,
    sitemapRouteV1,
    systemRouteV1,
    tagRouteV1
  )

  private def server(host: Ipv4Address, port: Port, httpApp: HttpApp[IO]): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(host)
      .withPort(port)
      .withHttpApp(httpApp)
      .withLogger(logger)
      .withShutdownTimeout(1.second)
      .build
  }

  def run(args: List[String]): IO[ExitCode] = {
    val host = Ipv4Address.fromString(Modules.config.http.host).getOrElse(ipv4"127.0.0.1")
    val port = Port.fromInt(Modules.config.http.port).getOrElse(port"9001")

    (for {
      _ <- logger.info(ApplicationInfo.asJson)
      _ <- IO(Modules.migrator.migrate(Modules.contentTypeService))
      routes <- router.withCors.map[Kleisli[IO, Request[IO], Response[IO]]](x => x.orNotFound)
      httpApp <- IO(new HttpAppBuilder(routes).build)
      server <- IO(
        server(host, port, httpApp)
          .use(_ => IO.never)
          .as(ExitCode.Success)
      )
    } yield server).flatMap(identity)

  }
}
