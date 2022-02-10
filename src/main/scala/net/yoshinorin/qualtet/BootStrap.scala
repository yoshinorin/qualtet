package net.yoshinorin.qualtet

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import com.github.benmanes.caffeine.cache.{Caffeine, Cache => CaffeineCache}
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.archives.DoobieArchiveRepository
import net.yoshinorin.qualtet.domains.models.articles.DoobieArticleRepository
import net.yoshinorin.qualtet.domains.models.authors.DoobieAuthorRepository
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, DoobieContentTypeRepository}
import net.yoshinorin.qualtet.domains.models.contents.{DoobieContentRepository, DoobieContentTaggingRepository}
import net.yoshinorin.qualtet.domains.models.externalResources.DoobieExternalResourceRepository
import net.yoshinorin.qualtet.domains.models.robots.DoobieRobotsRepository
import net.yoshinorin.qualtet.domains.models.sitemaps.{DoobieSitemapsRepository, Url}
import net.yoshinorin.qualtet.domains.models.tags.DoobieTagRepository
import net.yoshinorin.qualtet.domains.services.{ArchiveService, ArticleService, AuthorService, ContentService, ContentTypeService, SitemapService, TagService}
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
import net.yoshinorin.qualtet.utils.Cache
import pdi.jwt.JwtAlgorithm

import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import scala.io.StdIn

object BootStrap extends App {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  logger.info("booting...")

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  logger.info("dispatched: actorSystem")

  implicit val doobieContext: DoobieContext = new DoobieContext()

  logger.info("created: doobieContext")

  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes
  val signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance = new Jwt(JwtAlgorithm.RS256, keyPair, signature)

  logger.info("created: keyPair, signature and jwt instances")

  val authorRepository = new DoobieAuthorRepository
  val authorService: AuthorService = new AuthorService(authorRepository)

  val authService = new AuthService(authorService, jwtInstance)

  val contentTypeRepository = new DoobieContentTypeRepository
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(Config.cacheContentType, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache = new Cache[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService: ContentTypeService = new ContentTypeService(contentTypeRepository, contentTypeCache)

  val tagRepository = new DoobieTagRepository
  val tagService = new TagService(tagRepository)

  val contentTaggingRepository = new DoobieContentTaggingRepository

  val robotsRepository = new DoobieRobotsRepository

  val externalResourceRepository = new DoobieExternalResourceRepository

  val contentRepository = new DoobieContentRepository
  val contentService: ContentService =
    new ContentService(
      contentRepository,
      tagService,
      contentTaggingRepository,
      robotsRepository,
      externalResourceRepository,
      authorService,
      contentTypeService
    )

  val articleRepository = new DoobieArticleRepository
  val articleService: ArticleService = new ArticleService(articleRepository, contentTypeService)

  val archiveRepository = new DoobieArchiveRepository
  val archiveService: ArchiveService = new ArchiveService(archiveRepository, contentTypeService)

  val sitemapRepository = new DoobieSitemapsRepository
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(Config.cacheSitemap, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache = new Cache[String, Seq[Url]](sitemapCaffeinCache)
  val sitemapService = new SitemapService(sitemapRepository, sitemapCache)

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
