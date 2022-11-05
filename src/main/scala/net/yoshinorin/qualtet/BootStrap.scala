package net.yoshinorin.qualtet

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
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

// import scala.io.StdIn

@SuppressWarnings(Array("org.wartremover.warts.ScalaApp")) // Not yet migrate to Scala3
object BootStrap extends App {

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

  val httpServer: HttpServer =
    new HttpServer(
      homeRoute,
      apiStatusRoute,
      authRoute,
      authorRoute,
      contentRoute,
      tagRoute,
      articleRoute,
      archiveRoute,
      contentTypeRoute,
      sitemapRoute,
      feedRoute,
      cacheRoute
    )

  logger.info("starting http server...")

  httpServer.start(Config.httpHost, Config.httpPort).onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      logger.info(s"http server online at http://${address.getHostString}:${address.getPort}/")
    // NOTE: docker & sbt-revolver does not work if below codes are enabled.
    //       If do not user sbt-revolver when development below codes should be enable vice versa.
    /*
      StdIn.readLine()
      binding
        .unbind()
        .onComplete(_ => actorSystem.terminate())
     */
    case Failure(ex) =>
      println("Failed to bind HTTP endpoint, terminating system", ex)
      actorSystem.terminate()
  }
}
