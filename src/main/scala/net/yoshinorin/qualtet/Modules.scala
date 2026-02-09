package net.yoshinorin.qualtet

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import cats.effect.kernel.Resource
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.typelevel.log4cats.LoggerFactory as Log4CatsLoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory as Log4CatsSlf4jFactory
import org.typelevel.otel4s.trace.Tracer
import com.github.benmanes.caffeine.cache.{Cache as CaffeineCache, Caffeine}
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.config.ApplicationConfig
import net.yoshinorin.qualtet.domains.{ArticlesPagination, FeedsPagination, PaginationOps, TagsPagination}
import net.yoshinorin.qualtet.domains.archives.{ArchiveRepository, ArchiveRepositoryAdapter, ArchiveService}
import net.yoshinorin.qualtet.domains.articles.{ArticleRepository, ArticleRepositoryAdapter, ArticleService}
import net.yoshinorin.qualtet.domains.authors.{AuthorRepository, AuthorRepositoryAdapter, AuthorService}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.contentTypes.ContentType
import net.yoshinorin.qualtet.domains.contentTaggings.{ContentTaggingRepository, ContentTaggingRepositoryAdapter}
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
  ArchiveRoute as ArchiveRouteV1,
  ArticleRoute as ArticleRouteV1,
  AuthRoute as AuthRouteV1,
  AuthorRoute as AuthorRouteV1,
  CacheRoute as CacheRouteV1,
  ContentRoute as ContentRouteV1,
  ContentTypeRoute as ContentTypeRouteV1,
  FeedRoute as FeedRouteV1,
  SearchRoute as SearchRouteV1,
  SeriesRoute as SeriesRouteV1,
  SitemapRoute as SitemapRouteV1,
  SystemRoute as SystemRouteV1,
  TagRoute as TagRouteV1
}
import net.yoshinorin.qualtet.infrastructure.db.migrator.FlywayMigrator
import net.yoshinorin.qualtet.infrastructure.db.migrator.application.Migrator
import net.yoshinorin.qualtet.infrastructure.db.doobie.{DoobieExecuter, DoobieTransactor}
import net.yoshinorin.qualtet.infrastructure.versions.{V218Migrator, VersionMigrator, VersionRepository, VersionRepositoryAdapter, VersionService}
import net.yoshinorin.qualtet.infrastructure.telemetry.Otel

import pdi.jwt.JwtAlgorithm
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

object Modules {
  private val config = ApplicationConfig.load
  private val doobieTransactor: DoobieTransactor[Aux] = summon[DoobieTransactor[Aux]]

  def makeOtel(runtime: IORuntime): Option[Resource[IO, Tracer[IO]]] = Otel.initialize(runtime, config.otel)

  def transactorResource(maybeTracer: Option[Tracer[IO]]): Resource[IO, Transactor[IO]] = doobieTransactor.make(config.db, maybeTracer)

  given log4catsLogger: Log4CatsLoggerFactory[IO] = Log4CatsSlf4jFactory.create[IO]
}

class Modules(tx: Transactor[IO], maybeTracer: Option[Tracer[IO]] = None) {

  import Modules.log4catsLogger

  val config = Modules.config
  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(tx, maybeTracer)

  val flywayMigrator: FlywayMigrator = new FlywayMigrator(config.db)
  val migrator: Migrator = new Migrator()
  val v218Migrator: VersionMigrator[IO, ConnectionIO] = summon[VersionMigrator[IO, ConnectionIO]](using V218Migrator.V218)

  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair: KeyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes("UTF-8")
  val signature: Signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance: Jwt[IO] = new Jwt[IO](config.jwt, JwtAlgorithm.RS256, keyPair, signature)

  val authorRepository: AuthorRepository[ConnectionIO] = summon[AuthorRepository[ConnectionIO]]
  val authorRepositoryAdapter: AuthorRepositoryAdapter[ConnectionIO] = new AuthorRepositoryAdapter[ConnectionIO](authorRepository)
  val authorService = new AuthorService[IO, ConnectionIO](authorRepositoryAdapter)

  val authService = new AuthService[IO, ConnectionIO](authorService, jwtInstance)

  val contentTypeRepository: ContentTypeRepository[ConnectionIO] = summon[ContentTypeRepository[ConnectionIO]]
  val contentTypeRepositoryAdapter: ContentTypeRepositoryAdapter[ConnectionIO] = new ContentTypeRepositoryAdapter(contentTypeRepository)
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.contentType, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache: CacheModule[IO, String, ContentType] = new CacheModule[IO, String, ContentType](contentTypeCaffeinCache)
  val contentTypeService = new ContentTypeService[IO, ConnectionIO](contentTypeRepositoryAdapter, contentTypeCache)

  val robotsRepository: RobotsRepository[ConnectionIO] = summon[RobotsRepository[ConnectionIO]]
  val robotsRepositoryAdapter = new RobotsRepositoryAdapter(robotsRepository)

  val externalResourceRepository: ExternalResourceRepository[ConnectionIO] = summon[ExternalResourceRepository[ConnectionIO]]
  val externalResourceRepositoryAdapter = new ExternalResourceRepositoryAdapter(externalResourceRepository)

  val contentTaggingRepository: ContentTaggingRepository[ConnectionIO] = summon[ContentTaggingRepository[ConnectionIO]]
  val contentTaggingRepositoryAdapter = new ContentTaggingRepositoryAdapter[ConnectionIO](contentTaggingRepository)

  val tagRepository: TagRepository[ConnectionIO] = summon[TagRepository[ConnectionIO]]
  val tagsCaffeinCache: CaffeineCache[String, Seq[TagResponseModel]] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.tags, TimeUnit.SECONDS).build[String, Seq[TagResponseModel]]
  val tagsCache: CacheModule[IO, String, Seq[TagResponseModel]] = new CacheModule[IO, String, Seq[TagResponseModel]](tagsCaffeinCache)
  val tagRepositoryAdapter: TagRepositoryAdapter[ConnectionIO] = new TagRepositoryAdapter[ConnectionIO](tagRepository)
  val tagService = new TagService[IO, ConnectionIO](tagRepositoryAdapter, tagsCache, contentTaggingRepositoryAdapter)

  val searchRepository: SearchRepository[ConnectionIO] = summon[SearchRepository[ConnectionIO]]
  val searchService = new SearchService[IO, ConnectionIO](config.search, searchRepository)

  val articleRepository: ArticleRepository[ConnectionIO] = summon[ArticleRepository[ConnectionIO]]
  val articleRepositoryAdapter: ArticleRepositoryAdapter[ConnectionIO] = new ArticleRepositoryAdapter[ConnectionIO](articleRepository)
  val articlesPagination = summon[PaginationOps[ArticlesPagination]]
  val tagsPagination = summon[PaginationOps[TagsPagination]]
  val articleService = new ArticleService[IO, ConnectionIO](articleRepositoryAdapter, articlesPagination, tagsPagination, contentTypeService)

  val contentSerializingRepository: ContentSerializingRepository[ConnectionIO] = summon[ContentSerializingRepository[ConnectionIO]]
  val contentSerializingRepositoryAdapter: ContentSerializingRepositoryAdapter[ConnectionIO] = new ContentSerializingRepositoryAdapter(
    contentSerializingRepository
  )

  val seriesRepository: SeriesRepository[ConnectionIO] = summon[SeriesRepository[ConnectionIO]]
  val seriesRepositoryAdapter: SeriesRepositoryAdapter[ConnectionIO] = new SeriesRepositoryAdapter[ConnectionIO](seriesRepository)
  val seriesService = new SeriesService[IO, ConnectionIO](seriesRepositoryAdapter, contentSerializingRepositoryAdapter, articleService)

  val contentRepository: ContentRepository[ConnectionIO] = summon[ContentRepository[ConnectionIO]]
  val contentRepositoryAdapter: ContentRepositoryAdapter[ConnectionIO] = new ContentRepositoryAdapter[ConnectionIO](contentRepository)
  val contentService =
    new ContentService[IO, ConnectionIO](
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
  val archiveService = new ArchiveService[IO, ConnectionIO](archiveRepositoryAdapter, contentTypeService)

  val sitemapRepository: SitemapsRepository[ConnectionIO] = summon[SitemapsRepository[ConnectionIO]]
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.sitemap, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache: CacheModule[IO, String, Seq[Url]] = new CacheModule[IO, String, Seq[Url]](sitemapCaffeinCache)
  val sitemapRepositoryAdapter: SitemapRepositoryAdapter[ConnectionIO] = new SitemapRepositoryAdapter[ConnectionIO](sitemapRepository)
  val sitemapService = new SitemapService[IO, ConnectionIO](sitemapRepositoryAdapter, sitemapCache)

  val feedsPagination = summon[PaginationOps[FeedsPagination]]
  val feedCaffeinCache: CaffeineCache[String, ArticleWithCountResponseModel] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.feed, TimeUnit.SECONDS).build[String, ArticleWithCountResponseModel]
  val feedCache: CacheModule[IO, String, ArticleWithCountResponseModel] = new CacheModule[IO, String, ArticleWithCountResponseModel](feedCaffeinCache)
  val feedService = new FeedService[IO, ConnectionIO](feedsPagination, feedCache, articleService)

  val cacheService = new CacheService[IO, ConnectionIO](
    sitemapService,
    tagService,
    contentTypeService,
    feedService
  )

  val versionRepository: VersionRepository[ConnectionIO] = summon[VersionRepository[ConnectionIO]]
  val versionRepositoryAdapter: VersionRepositoryAdapter[ConnectionIO] = new VersionRepositoryAdapter[ConnectionIO](versionRepository)
  val versionService = new VersionService[IO, ConnectionIO](versionRepositoryAdapter)

  val authProvider = new AuthProvider[ConnectionIO](authService)
  val corsProvider = new CorsProvider(config.cors)

  val archiveRouteV1 = new ArchiveRouteV1[ConnectionIO](archiveService)
  val articleRouteV1 = new ArticleRouteV1[ConnectionIO](articleService)
  val authorRouteV1 = new AuthorRouteV1[ConnectionIO](authorService)
  val authRouteV1 = new AuthRouteV1[ConnectionIO](authService)
  val cacheRouteV1 = new CacheRouteV1[ConnectionIO](authProvider, cacheService)
  val contentTypeRouteV1 = new ContentTypeRouteV1[ConnectionIO](contentTypeService)
  val contentRouteV1 = new ContentRouteV1[ConnectionIO](authProvider, contentService)
  val feedRouteV1 = new FeedRouteV1[ConnectionIO](feedService)
  val homeRoute: HomeRoute = new HomeRoute()
  val searchRouteV1 = new SearchRouteV1[ConnectionIO](searchService)
  val seriesRouteV1 = new SeriesRouteV1[ConnectionIO](authProvider, seriesService)
  val sitemapRouteV1 = new SitemapRouteV1[ConnectionIO](sitemapService)
  val systemRouteV1 = new SystemRouteV1(config.http.endpoints.system)
  val tagRouteV1 = new TagRouteV1[ConnectionIO](authProvider, tagService, articleService)

  val router = new net.yoshinorin.qualtet.http.Router[ConnectionIO](
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
