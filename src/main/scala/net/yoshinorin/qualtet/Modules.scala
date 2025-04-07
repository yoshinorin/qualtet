package net.yoshinorin.qualtet

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.typelevel.log4cats.{LoggerFactory => Log4CatsLoggerFactory}
import org.typelevel.log4cats.slf4j.{Slf4jFactory => Log4CatsSlf4jFactory}
import com.github.benmanes.caffeine.cache.{Cache => CaffeineCache, Caffeine}
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.config.ApplicationConfig
import net.yoshinorin.qualtet.domains.{ArticlesPagination, FeedsPagination, PaginationOps, TagsPagination}
import net.yoshinorin.qualtet.domains.archives.{ArchiveRepository, ArchiveRepositoryAdapter, ArchiveService}
import net.yoshinorin.qualtet.domains.articles.{ArticleRepository, ArticleRepositoryAdapter, ArticleService}
import net.yoshinorin.qualtet.domains.authors.{AuthorRepository, AuthorRepositoryAdapter, AuthorService}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.contentTypes.ContentType
import net.yoshinorin.qualtet.domains.contentTaggings.{ContentTaggingRepository, ContentTaggingRepositoryAdapter, ContentTaggingService}
import net.yoshinorin.qualtet.domains.contents.{ContentRepository, ContentRepositoryAdapter, ContentService}
import net.yoshinorin.qualtet.domains.contentSerializing.{ContentSerializingRepository, ContentSerializingRepositoryAdapter}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeRepository, ContentTypeRepositoryAdapter}
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceRepository, ExternalResourceRepositoryAdapter}
import net.yoshinorin.qualtet.domains.robots.{RobotsRepository, RobotsRepositoryAdapter}
import net.yoshinorin.qualtet.domains.search.{SearchRepository, SearchService}
import net.yoshinorin.qualtet.domains.series.{SeriesRepository, SeriesRepositoryAdapter, SeriesService}
import net.yoshinorin.qualtet.domains.sitemaps.{SitemapRepositoryAdapter, SitemapService, SitemapsRepository, Url}
import net.yoshinorin.qualtet.domains.tags.{TagRepository, TagRepositoryAdapter, TagResponseModel, TagService}
import net.yoshinorin.qualtet.auth.Signature
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.domains.articles.ArticleWithCountResponseModel
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
import net.yoshinorin.qualtet.infrastructure.db.doobie.{DoobieExecuter, DoobieTransactor}

import pdi.jwt.JwtAlgorithm
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

object Modules {
  private val config = ApplicationConfig.load
  private val doobieTransactor: DoobieTransactor[Aux] = summon[DoobieTransactor[Aux]]

  val transactorResource = doobieTransactor.make(config.db)
  given log4catsLogger: Log4CatsLoggerFactory[IO] = Log4CatsSlf4jFactory.create[IO]
}

class Modules(tx: Transactor[IO]) {

  import Modules.log4catsLogger

  val config = Modules.config
  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(tx)

  val migrator: Migrator = new Migrator(config.db)

  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair: KeyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes("UTF-8")
  val signature: Signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance: Jwt[IO] = new Jwt[IO](config.jwt, JwtAlgorithm.RS256, keyPair, signature)

  val authorRepository: AuthorRepository[ConnectionIO] = summon[AuthorRepository[ConnectionIO]]
  val authorRepositoryAdapter: AuthorRepositoryAdapter[ConnectionIO] = new AuthorRepositoryAdapter[ConnectionIO](authorRepository)
  val authorService = new AuthorService(authorRepositoryAdapter)

  val authService = new AuthService(authorService, jwtInstance)

  val contentTypeRepository: ContentTypeRepository[ConnectionIO] = summon[ContentTypeRepository[ConnectionIO]]
  val contentTypeRepositoryAdapter: ContentTypeRepositoryAdapter[ConnectionIO] = new ContentTypeRepositoryAdapter(contentTypeRepository)
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.contentType, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache: CacheModule[IO, String, ContentType] = new CacheModule[IO, String, ContentType](contentTypeCaffeinCache)
  val contentTypeService = new ContentTypeService(contentTypeRepositoryAdapter, contentTypeCache)

  val robotsRepository: RobotsRepository[ConnectionIO] = summon[RobotsRepository[ConnectionIO]]
  val robotsRepositoryAdapter = new RobotsRepositoryAdapter(robotsRepository)

  val externalResourceRepository: ExternalResourceRepository[ConnectionIO] = summon[ExternalResourceRepository[ConnectionIO]]
  val externalResourceRepositoryAdapter = new ExternalResourceRepositoryAdapter(externalResourceRepository)

  val contentTaggingRepository: ContentTaggingRepository[ConnectionIO] = summon[ContentTaggingRepository[ConnectionIO]]
  val contentTaggingRepositoryAdapter = new ContentTaggingRepositoryAdapter[ConnectionIO](contentTaggingRepository)
  val contentTaggingService = new ContentTaggingService(contentTaggingRepositoryAdapter)

  val tagRepository: TagRepository[ConnectionIO] = summon[TagRepository[ConnectionIO]]
  val tagsCaffeinCache: CaffeineCache[String, Seq[TagResponseModel]] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.tags, TimeUnit.SECONDS).build[String, Seq[TagResponseModel]]
  val tagsCache: CacheModule[IO, String, Seq[TagResponseModel]] = new CacheModule[IO, String, Seq[TagResponseModel]](tagsCaffeinCache)
  val tagRepositoryAdapter: TagRepositoryAdapter[ConnectionIO] = new TagRepositoryAdapter[ConnectionIO](tagRepository)
  val tagService = new TagService(tagRepositoryAdapter, tagsCache, contentTaggingRepositoryAdapter)

  val searchRepository: SearchRepository[ConnectionIO] = summon[SearchRepository[ConnectionIO]]
  val searchService = new SearchService(config.search, searchRepository)

  val articleRepository: ArticleRepository[ConnectionIO] = summon[ArticleRepository[ConnectionIO]]
  val articleRepositoryAdapter: ArticleRepositoryAdapter[ConnectionIO] = new ArticleRepositoryAdapter[ConnectionIO](articleRepository)
  val articlesPagination = summon[PaginationOps[ArticlesPagination]]
  val tagsPagination = summon[PaginationOps[TagsPagination]]
  val articleService = new ArticleService(articleRepositoryAdapter, articlesPagination, tagsPagination, contentTypeService)

  val contentSerializingRepository: ContentSerializingRepository[ConnectionIO] = summon[ContentSerializingRepository[ConnectionIO]]
  val contentSerializingRepositoryAdapter: ContentSerializingRepositoryAdapter[ConnectionIO] = new ContentSerializingRepositoryAdapter(
    contentSerializingRepository
  )

  val seriesRepository: SeriesRepository[ConnectionIO] = summon[SeriesRepository[ConnectionIO]]
  val seriesRepositoryAdapter: SeriesRepositoryAdapter[ConnectionIO] = new SeriesRepositoryAdapter[ConnectionIO](seriesRepository)
  val seriesService = new SeriesService(seriesRepositoryAdapter, contentSerializingRepositoryAdapter, articleService)

  val contentRepository: ContentRepository[ConnectionIO] = summon[ContentRepository[ConnectionIO]]
  val contentRepositoryAdapter: ContentRepositoryAdapter[ConnectionIO] = new ContentRepositoryAdapter[ConnectionIO](contentRepository)
  val contentService =
    new ContentService(
      contentRepositoryAdapter,
      tagRepositoryAdapter,
      tagService,
      contentTaggingRepositoryAdapter,
      robotsRepositoryAdapter,
      externalResourceRepositoryAdapter,
      authorService,
      contentTypeService,
      seriesRepositoryAdapter,
      seriesService,
      contentSerializingRepositoryAdapter
    )

  val archiveRepository: ArchiveRepository[ConnectionIO] = summon[ArchiveRepository[ConnectionIO]]
  val archiveRepositoryAdapter: ArchiveRepositoryAdapter[ConnectionIO] = new ArchiveRepositoryAdapter[ConnectionIO](archiveRepository)
  val archiveService = new ArchiveService(archiveRepositoryAdapter, contentTypeService)

  val sitemapRepository: SitemapsRepository[ConnectionIO] = summon[SitemapsRepository[ConnectionIO]]
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.sitemap, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache: CacheModule[IO, String, Seq[Url]] = new CacheModule[IO, String, Seq[Url]](sitemapCaffeinCache)
  val sitemapRepositoryAdapter: SitemapRepositoryAdapter[ConnectionIO] = new SitemapRepositoryAdapter[ConnectionIO](sitemapRepository)
  val sitemapService = new SitemapService(sitemapRepositoryAdapter, sitemapCache)

  val feedsPagination = summon[PaginationOps[FeedsPagination]]
  val feedCaffeinCache: CaffeineCache[String, ArticleWithCountResponseModel] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.feed, TimeUnit.SECONDS).build[String, ArticleWithCountResponseModel]
  val feedCache: CacheModule[IO, String, ArticleWithCountResponseModel] = new CacheModule[IO, String, ArticleWithCountResponseModel](feedCaffeinCache)
  val feedService = new FeedService(feedsPagination, feedCache, articleService)

  val cacheService = new CacheService(
    sitemapService,
    tagService,
    contentTypeService,
    feedService
  )

  val authProvider = new AuthProvider(authService)
  val corsProvider = new CorsProvider(config.cors)

  val archiveRouteV1 = new ArchiveRouteV1(archiveService)
  val articleRouteV1 = new ArticleRouteV1(articleService)
  val authorRouteV1 = new AuthorRouteV1(authorService)
  val authRouteV1 = new AuthRouteV1(authService)
  val cacheRouteV1 = new CacheRouteV1(authProvider, cacheService)
  val contentTypeRouteV1 = new ContentTypeRouteV1(contentTypeService)
  val contentRouteV1 = new ContentRouteV1(authProvider, contentService)
  val feedRouteV1 = new FeedRouteV1(feedService)
  val homeRoute: HomeRoute = new HomeRoute()
  val searchRouteV1 = new SearchRouteV1(searchService)
  val seriesRouteV1 = new SeriesRouteV1(authProvider, seriesService)
  val sitemapRouteV1 = new SitemapRouteV1(sitemapService)
  val systemRouteV1 = new SystemRouteV1(config.http.endpoints.system)
  val tagRouteV1 = new TagRouteV1(authProvider, tagService, articleService)

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
