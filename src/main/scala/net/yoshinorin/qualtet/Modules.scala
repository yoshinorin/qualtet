package net.yoshinorin.qualtet

import cats.effect.IO
import com.github.benmanes.caffeine.cache.{Caffeine, Cache => CaffeineCache}
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.config.ApplicationConfig
import net.yoshinorin.qualtet.domains.archives.{ArchiveRepositoryDoobieInterpreter, ArchiveService}
import net.yoshinorin.qualtet.domains.articles.{ArticleRepositoryDoobieInterpreter, ArticleService}
import net.yoshinorin.qualtet.domains.authors.{AuthorRepositoryDoobieInterpreter, AuthorService}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeRepositoryDoobieInterpreter, ContentType, ContentTypeService}
import net.yoshinorin.qualtet.domains.contentTaggings.{ContentTaggingRepositoryDoobieInterpretere, ContentTaggingService}
import net.yoshinorin.qualtet.domains.contents.{ContentRepositoryDoobieInterpreter, ContentService}
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceRepositoryDoobieInterpreter, ExternalResourceService}
import net.yoshinorin.qualtet.domains.robots.{RobotsRepositoryDoobieInterpreter, RobotsService}
import net.yoshinorin.qualtet.domains.search.{SearchRepositoryDoobieInterpreter, SearchService}
import net.yoshinorin.qualtet.domains.series.{SeriesRepositoryDoobieInterpreter, SeriesService}
import net.yoshinorin.qualtet.domains.sitemaps.{SitemapsRepositoryDoobieInterpreter, SitemapService, Url}
import net.yoshinorin.qualtet.domains.tags.{TagRepositoryDoobieInterpreter, TagService}
import net.yoshinorin.qualtet.auth.Signature
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount
import net.yoshinorin.qualtet.infrastructure.db.Migrator
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieTransactor

import pdi.jwt.JwtAlgorithm
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

object Modules {

  val config = ApplicationConfig.load

  given dbContext: DoobieTransactor = new DoobieTransactor(config.db)
  val migrator: Migrator = new Migrator(config.db)

  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair: KeyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes("UTF-8")
  val signature: Signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance: Jwt = new Jwt(config.jwt, JwtAlgorithm.RS256, keyPair, signature)

  val authorRepository: AuthorRepositoryDoobieInterpreter = new AuthorRepositoryDoobieInterpreter()
  val authorService = new AuthorService(authorRepository)

  val authService = new AuthService(authorService, jwtInstance)

  val contentTypeRepository: ContentTypeRepositoryDoobieInterpreter = new ContentTypeRepositoryDoobieInterpreter()
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(config.cache.contentType, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache: CacheModule[String, ContentType] = new CacheModule[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService = new ContentTypeService(contentTypeRepository, contentTypeCache)

  val robotsRepository: RobotsRepositoryDoobieInterpreter = new RobotsRepositoryDoobieInterpreter()
  val robotsService = new RobotsService(robotsRepository)

  val externalResourceRepository: ExternalResourceRepositoryDoobieInterpreter = new ExternalResourceRepositoryDoobieInterpreter()
  val externalResourceService = new ExternalResourceService(externalResourceRepository)

  val contentTaggingRepository: ContentTaggingRepositoryDoobieInterpretere = new ContentTaggingRepositoryDoobieInterpretere()
  val contentTaggingService = new ContentTaggingService(contentTaggingRepository)

  val tagRepository: TagRepositoryDoobieInterpreter = new TagRepositoryDoobieInterpreter()
  val tagService = new TagService(tagRepository, contentTaggingService)

  val searchRepository: SearchRepositoryDoobieInterpreter = new SearchRepositoryDoobieInterpreter()
  val searchService = new SearchService(config.search, searchRepository)

  val seriesRepository: SeriesRepositoryDoobieInterpreter = new SeriesRepositoryDoobieInterpreter()
  val seriesService = new SeriesService(seriesRepository)

  val contentRepository: ContentRepositoryDoobieInterpreter = new ContentRepositoryDoobieInterpreter()
  val contentService =
    new ContentService(
      contentRepository,
      tagService,
      contentTaggingService,
      robotsService,
      externalResourceService,
      authorService,
      contentTypeService,
      seriesService
    )

  val articleRepository = new ArticleRepositoryDoobieInterpreter()
  val articleService = new ArticleService(articleRepository, contentTypeService)

  val archiveRepository: ArchiveRepositoryDoobieInterpreter = new ArchiveRepositoryDoobieInterpreter()
  val archiveService = new ArchiveService(archiveRepository, contentTypeService)

  val sitemapRepository: SitemapsRepositoryDoobieInterpreter = new SitemapsRepositoryDoobieInterpreter()
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
}
