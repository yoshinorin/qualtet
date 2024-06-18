package net.yoshinorin.qualtet

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor.Aux
import org.typelevel.log4cats.{LoggerFactory => Log4CatsLoggerFactory}
import org.typelevel.log4cats.slf4j.{Slf4jFactory => Log4CatsSlf4jFactory}
import com.github.benmanes.caffeine.cache.{Cache => CaffeineCache, Caffeine}
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.config.ApplicationConfig
import net.yoshinorin.qualtet.domains.archives.{ArchiveRepository, ArchiveService}
import net.yoshinorin.qualtet.domains.articles.{ArticleRepository, ArticleService}
import net.yoshinorin.qualtet.domains.authors.{AuthorRepository, AuthorService}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentType, ContentTypeService}
import net.yoshinorin.qualtet.domains.contentTaggings.{ContentTaggingRepository, ContentTaggingService}
import net.yoshinorin.qualtet.domains.contents.{ContentRepository, ContentService}
import net.yoshinorin.qualtet.domains.contentSerializing.{ContentSerializingRepository, ContentSerializingService}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeRepository
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceRepository, ExternalResourceService}
import net.yoshinorin.qualtet.domains.robots.{RobotsRepository, RobotsService}
import net.yoshinorin.qualtet.domains.search.{SearchRepository, SearchService}
import net.yoshinorin.qualtet.domains.series.{SeriesRepository, SeriesService}
import net.yoshinorin.qualtet.domains.sitemaps.{SitemapService, SitemapsRepository, Url}
import net.yoshinorin.qualtet.domains.tags.{TagRepository, TagService}
import net.yoshinorin.qualtet.auth.Signature
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount
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
import net.yoshinorin.qualtet.infrastructure.db.Migrator
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter

import pdi.jwt.JwtAlgorithm
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import doobie.util.transactor.Transactor
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieTransactor

object Modules {

  val config = ApplicationConfig.load
  val doobieTransactor: DoobieTransactor[Aux] = summon[DoobieTransactor[Aux]]

  given log4catsLogger: Log4CatsLoggerFactory[IO] = Log4CatsSlf4jFactory.create[IO]
  given dbContext: DoobieExecuter = new DoobieExecuter(doobieTransactor.make(config.db))
  val migrator: Migrator = new Migrator(config.db)

  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair: KeyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes("UTF-8")
  val signature: Signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance: Jwt = new Jwt(config.jwt, JwtAlgorithm.RS256, keyPair, signature)

  val authorRepository: AuthorRepository[ConnectionIO] = summon[AuthorRepository[ConnectionIO]]
  val authorService = new AuthorService(authorRepository)

  val authService = new AuthService(authorService, jwtInstance)

  val contentTypeRepository: ContentTypeRepository[ConnectionIO] = summon[ContentTypeRepository[ConnectionIO]]
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.contentType, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache: CacheModule[String, ContentType] = new CacheModule[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService = new ContentTypeService(contentTypeRepository, contentTypeCache)

  val robotsRepository: RobotsRepository[ConnectionIO] = summon[RobotsRepository[ConnectionIO]]
  val robotsService = new RobotsService(robotsRepository)

  val externalResourceRepository: ExternalResourceRepository[ConnectionIO] = summon[ExternalResourceRepository[ConnectionIO]]
  val externalResourceService = new ExternalResourceService(externalResourceRepository)

  val contentTaggingRepository: ContentTaggingRepository[ConnectionIO] = summon[ContentTaggingRepository[ConnectionIO]]
  val contentTaggingService = new ContentTaggingService(contentTaggingRepository)

  val tagRepository: TagRepository[ConnectionIO] = summon[TagRepository[ConnectionIO]]
  val tagService = new TagService(tagRepository, contentTaggingService)

  val searchRepository: SearchRepository[ConnectionIO] = summon[SearchRepository[ConnectionIO]]
  val searchService = new SearchService(config.search, searchRepository)

  val articleRepository: ArticleRepository[ConnectionIO] = summon[ArticleRepository[ConnectionIO]]
  val articleService = new ArticleService(articleRepository, contentTypeService)

  val seriesRepository: SeriesRepository[ConnectionIO] = summon[SeriesRepository[ConnectionIO]]
  val seriesService = new SeriesService(seriesRepository, articleService)

  val contentSerializingRepository: ContentSerializingRepository[ConnectionIO] = summon[ContentSerializingRepository[ConnectionIO]]
  val contentSerializingService = new ContentSerializingService(contentSerializingRepository)

  val contentRepository: ContentRepository[ConnectionIO] = summon[ContentRepository[ConnectionIO]]
  val contentService =
    new ContentService(
      contentRepository,
      tagService,
      contentTaggingService,
      robotsService,
      externalResourceService,
      authorService,
      contentTypeService,
      seriesService,
      contentSerializingService
    )

  val archiveRepository: ArchiveRepository[ConnectionIO] = summon[ArchiveRepository[ConnectionIO]]
  val archiveService = new ArchiveService(archiveRepository, contentTypeService)

  val sitemapRepository: SitemapsRepository[ConnectionIO] = summon[SitemapsRepository[ConnectionIO]]
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.sitemap, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache: CacheModule[String, Seq[Url]] = new CacheModule[String, Seq[Url]](sitemapCaffeinCache)
  val sitemapService = new SitemapService(sitemapRepository, sitemapCache)

  val feedCaffeinCache: CaffeineCache[String, ResponseArticleWithCount] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.feed, TimeUnit.SECONDS).build[String, ResponseArticleWithCount]
  val feedCache: CacheModule[String, ResponseArticleWithCount] = new CacheModule[String, ResponseArticleWithCount](feedCaffeinCache)
  val feedService = new FeedService(feedCache, articleService)

  val cacheService = new CacheService(
    sitemapService,
    contentTypeService,
    feedService
  )

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

}
