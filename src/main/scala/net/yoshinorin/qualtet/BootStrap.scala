package net.yoshinorin.qualtet

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import com.github.benmanes.caffeine.cache.{Caffeine, Cache => CaffeineCache}
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.archives.DoobieArchiveRepository
import net.yoshinorin.qualtet.domains.models.articles.DoobieArticleRepository
import net.yoshinorin.qualtet.domains.models.authors.DoobieAuthorRepository
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, DoobieContentTypeRepository}
import net.yoshinorin.qualtet.domains.models.contents.DoobieContentRepository
import net.yoshinorin.qualtet.domains.models.robots.DoobieRobotsRepository
import net.yoshinorin.qualtet.domains.models.sitemaps.{DoobieSitemapsRepository, Url}
import net.yoshinorin.qualtet.domains.services.{ArchiveService, ArticleService, AuthorService, ContentService, ContentTypeService, SitemapService}
import net.yoshinorin.qualtet.http.routes.{
  ApiStatusRoute,
  ArchiveRoute,
  ArticleRoute,
  AuthRoute,
  AuthorRoute,
  ContentRoute,
  ContentTypeRoute,
  HomeRoute,
  SitemapRoute
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

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  implicit val doobieContext: DoobieContext = new DoobieContext()

  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes
  val signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance = new Jwt(JwtAlgorithm.RS256, keyPair, signature)

  val authorRepository = new DoobieAuthorRepository(doobieContext)
  val authorService: AuthorService = new AuthorService(authorRepository)

  val authService = new AuthService(authorService, jwtInstance)

  val contentTypeRepository = new DoobieContentTypeRepository(doobieContext)
  // TODO: from config for cache options
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache = new Cache[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService: ContentTypeService = new ContentTypeService(contentTypeRepository, contentTypeCache)

  val robotsRepository = new DoobieRobotsRepository(doobieContext)

  val contentRepository = new DoobieContentRepository(doobieContext)
  val contentService: ContentService = new ContentService(contentRepository, robotsRepository, authorService, contentTypeService)

  val articleRepository = new DoobieArticleRepository(doobieContext)
  val articleService: ArticleService = new ArticleService(articleRepository, contentTypeService)

  val archiveRepository = new DoobieArchiveRepository(doobieContext)
  val archiveService: ArchiveService = new ArchiveService(archiveRepository, contentTypeService)

  val sitemapRepository = new DoobieSitemapsRepository(doobieContext)
  // TODO: from inf cache
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache = new Cache[String, Seq[Url]](sitemapCaffeinCache)
  val sitemapService = new SitemapService(sitemapRepository, sitemapCache)

  val homeRoute: HomeRoute = new HomeRoute()
  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val authRoute: AuthRoute = new AuthRoute(authService)
  val authorRoute: AuthorRoute = new AuthorRoute(authorService)
  val contentRoute: ContentRoute = new ContentRoute(authService, contentService)
  val articleRoute: ArticleRoute = new ArticleRoute(articleService)
  val archiveRoute: ArchiveRoute = new ArchiveRoute(archiveService)
  val contentTypeRoute: ContentTypeRoute = new ContentTypeRoute(contentTypeService)
  val sitemapRoute: SitemapRoute = new SitemapRoute(sitemapService)

  Migration.migrate(contentTypeService)

  val httpServer: HttpServer =
    new HttpServer(homeRoute, apiStatusRoute, authRoute, authorRoute, contentRoute, articleRoute, archiveRoute, contentTypeRoute, sitemapRoute)

  httpServer.start(Config.httpHost, Config.httpPort).onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      println(s"Server online at http://${address.getHostString}:${address.getPort}/")
      // TODO: delete comment out when create docker image
      StdIn.readLine()
      binding
        .unbind()
        .onComplete(_ => actorSystem.terminate())
    case Failure(ex) =>
      println("Failed to bind HTTP endpoint, terminating system", ex)
      actorSystem.terminate()
  }
}
