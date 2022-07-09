package net.yoshinorin.qualtet

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import com.github.benmanes.caffeine.cache.{Caffeine, Cache => CaffeineCache}
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.authors.AuthorService
import net.yoshinorin.qualtet.domains.contentTypes.{ContentType, ContentTypeService}
import net.yoshinorin.qualtet.domains.contents.ContentService
import net.yoshinorin.qualtet.domains.externalResources.ExternalResourceService
import net.yoshinorin.qualtet.domains.robots.RobotsService
import net.yoshinorin.qualtet.domains.sitemaps.{SitemapService, Url}
import net.yoshinorin.qualtet.domains.tags.TagService
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
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import pdi.jwt.JwtAlgorithm

import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import net.yoshinorin.qualtet.auth.Signature
// import scala.io.StdIn

object BootStrap extends App {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  logger.info("booting...")

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  logger.info("dispatched: actorSystem")

  implicit val doobieContext: DoobieContext = new DoobieContext()

  logger.info("created: doobieContext")

  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair: KeyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes
  val signature: Signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance: Jwt = new Jwt(JwtAlgorithm.RS256, keyPair, signature)

  logger.info("created: keyPair, signature and jwt instances")

  val authorService: AuthorService = new AuthorService()(doobieContext)

  val authService: AuthService = new AuthService(authorService, jwtInstance)

  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(Config.cacheContentType, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache: CacheModule[String, ContentType] = new CacheModule[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService: ContentTypeService = new ContentTypeService(contentTypeCache)(doobieContext)

  val tagService: TagService = new TagService()(doobieContext)
  val robotsService: RobotsService = new RobotsService()
  val externalResourceService: ExternalResourceService = new ExternalResourceService()

  val contentService: ContentService =
    new ContentService(
      tagService,
      robotsService,
      externalResourceService,
      authorService,
      contentTypeService
    )(doobieContext)

  val articleService: ArticleService = new ArticleService(contentTypeService)(doobieContext)

  val archiveService: ArchiveService = new ArchiveService(contentTypeService)(doobieContext)

  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(Config.cacheSitemap, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache: CacheModule[String, Seq[Url]] = new CacheModule[String, Seq[Url]](sitemapCaffeinCache)
  val sitemapService: SitemapService = new SitemapService(sitemapCache)(doobieContext)

  val homeRoute: HomeRoute = new HomeRoute()
  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val authRoute: AuthRoute = new AuthRoute(authService)
  val authorRoute: AuthorRoute = new AuthorRoute(authorService)
  val contentRoute: ContentRoute = new ContentRoute(authService, contentService)
  val tagRoute: TagRoute = new TagRoute(tagService, articleService)
  val articleRoute: ArticleRoute = new ArticleRoute(articleService)
  val archiveRoute: ArchiveRoute = new ArchiveRoute(archiveService)
  val contentTypeRoute: ContentTypeRoute = new ContentTypeRoute(contentTypeService)
  val sitemapRoute: SitemapRoute = new SitemapRoute(sitemapService)
  val feedRoute: FeedRoute = new FeedRoute(articleService)

  logger.info("created all instances")

  Migration.migrate(contentTypeService)

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
      feedRoute
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
