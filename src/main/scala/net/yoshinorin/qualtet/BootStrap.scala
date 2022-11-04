package net.yoshinorin.qualtet

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import cats._
import cats.implicits._
import cats.effect.IOApp
import cats.effect.ExitCode
import cats.data.Kleisli
import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import com.github.benmanes.caffeine.cache.{Caffeine, Cache => CaffeineCache}
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.config.Config
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
import net.yoshinorin.qualtet.http.HttpServer
import net.yoshinorin.qualtet.infrastructure.db.Migration
import net.yoshinorin.qualtet.http.routes.CacheRoute
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount
import org.http4s.server.Router
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
// import scala.io.StdIn

@SuppressWarnings(Array("org.wartremover.warts.ScalaApp")) // Not yet migrate to Scala3
object BootStrap extends IOApp {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  logger.info("booting...")

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  logger.info("dispatched: actorSystem")

  val homeRoute: HomeRoute = new HomeRoute()
  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val authRoute: AuthRoute = new AuthRoute(Modules.authService)
  val authorRoute: AuthorRoute = new AuthorRoute(Modules.authorService)
  val contentRoute: ContentRoute = new ContentRoute(Modules.authService, Modules.contentService)
  val tagRoute: TagRoute = new TagRoute(Modules.authService, Modules.tagService, Modules.articleService)
  val articleRoute: ArticleRoute = new ArticleRoute(Modules.articleService)
  val archiveRoute: ArchiveRoute = new ArchiveRoute(Modules.archiveService)
  val contentTypeRoute: ContentTypeRoute = new ContentTypeRoute(Modules.contentTypeService)
  val sitemapRoute: SitemapRoute = new SitemapRoute(Modules.sitemapService)
  val feedRoute: FeedRoute = new FeedRoute(Modules.feedService)
  val cacheRoute: CacheRoute = new CacheRoute(Modules.authService, Modules.cacheService)

  logger.info("created all instances")

  Migration.migrate(Modules.contentTypeService)

  val homeRoute: HomeRoute = new HomeRoute()
  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val archiveRoute: ArchiveRoute = new ArchiveRoute(archiveService)
  val articleRoute: ArticleRoute = new ArticleRoute(articleService)
  val cacheRoute: CacheRoute = new CacheRoute(authService, cacheService)

  // TOOD: move somewhere
  // val routes: Kleisli[IO, Request[IO], Response[IO]] = apiStatusRoute.route <+> homeRoute.route
  val httpApp = Router(
    "/" -> homeRoute.route,
    "/" -> apiStatusRoute.route,
    "/" -> archiveRoute.route,
    "/" -> articleRoute.route,
    "/" -> cacheRoute.route
  ).orNotFound

  def run(args: List[String]): IO[ExitCode] = {

    logger.info("starting http server...")
    EmberServerBuilder
      .default[IO]
      .withHost(Ipv4Address.fromString(Config.httpHost).get)
      .withPort(Port.fromInt(Config.httpPort).get)
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
