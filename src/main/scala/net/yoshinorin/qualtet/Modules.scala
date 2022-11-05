package net.yoshinorin.qualtet

import com.github.benmanes.caffeine.cache.{Caffeine, Cache => CaffeineCache}
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.archives.{DoobieArchiveRepository, ArchiveService}
import net.yoshinorin.qualtet.domains.articles.{DoobieArticleRepository, ArticleService}
import net.yoshinorin.qualtet.domains.authors.{DoobieAuthorRepository, AuthorService}
import net.yoshinorin.qualtet.domains.contentTypes.{DoobieContentTypeRepository, ContentType, ContentTypeService}
import net.yoshinorin.qualtet.domains.contentTaggings.{DoobieContentTaggingRepository, ContentTaggingService}
import net.yoshinorin.qualtet.domains.contents.{DoobieContentRepository, ContentService}
import net.yoshinorin.qualtet.domains.externalResources.{DoobieExternalResourceRepository, ExternalResourceService}
import net.yoshinorin.qualtet.domains.robots.{DoobieRobotsRepository, RobotsService}
import net.yoshinorin.qualtet.domains.sitemaps.{DoobieSitemapsRepository, SitemapService, Url}
import net.yoshinorin.qualtet.domains.tags.{DoobieTagRepository, TagService}
import net.yoshinorin.qualtet.auth.Signature
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

import pdi.jwt.JwtAlgorithm
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

object Modules {

  implicit val doobieContext: DoobieContext = new DoobieContext()

  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair: KeyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes("UTF-8")
  val signature: Signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance: Jwt = new Jwt(JwtAlgorithm.RS256, keyPair, signature)

  val authorRepository: DoobieAuthorRepository = new DoobieAuthorRepository()
  val authorService: AuthorService = new AuthorService(authorRepository)(doobieContext)

  val authService: AuthService = new AuthService(authorService, jwtInstance)

  val contentTypeRepository: DoobieContentTypeRepository = new DoobieContentTypeRepository()
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(Config.cacheContentType, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache: CacheModule[String, ContentType] = new CacheModule[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService: ContentTypeService = new ContentTypeService(contentTypeRepository, contentTypeCache)(doobieContext)

  val robotsRepository: DoobieRobotsRepository = new DoobieRobotsRepository()
  val robotsService: RobotsService = new RobotsService(robotsRepository)

  val externalResourceRepository: DoobieExternalResourceRepository = new DoobieExternalResourceRepository()
  val externalResourceService: ExternalResourceService = new ExternalResourceService(externalResourceRepository)

  val contentTaggingRepository: DoobieContentTaggingRepository = new DoobieContentTaggingRepository()
  val contentTaggingService: ContentTaggingService = new ContentTaggingService(contentTaggingRepository)(doobieContext)

  val tagRepository: DoobieTagRepository = new DoobieTagRepository()
  val tagService: TagService = new TagService(tagRepository, contentTaggingService)(doobieContext)

  val contentRepository: DoobieContentRepository = new DoobieContentRepository()
  val contentService: ContentService =
    new ContentService(
      contentRepository,
      tagService,
      contentTaggingService,
      robotsService,
      externalResourceService,
      authorService,
      contentTypeService
    )(doobieContext)

  val articleRepository: DoobieArticleRepository = new DoobieArticleRepository()
  val articleService: ArticleService = new ArticleService(articleRepository, contentTypeService)(doobieContext)

  val archiveRepository: DoobieArchiveRepository = new DoobieArchiveRepository()
  val archiveService: ArchiveService = new ArchiveService(archiveRepository, contentTypeService)(doobieContext)

  val sitemapRepository: DoobieSitemapsRepository = new DoobieSitemapsRepository()
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(Config.cacheSitemap, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache: CacheModule[String, Seq[Url]] = new CacheModule[String, Seq[Url]](sitemapCaffeinCache)
  val sitemapService: SitemapService = new SitemapService(sitemapRepository, sitemapCache)(doobieContext)

  val feedCaffeinCache: CaffeineCache[String, ResponseArticleWithCount] =
    Caffeine.newBuilder().expireAfterAccess(Config.cacheSitemap, TimeUnit.SECONDS).build[String, ResponseArticleWithCount]
  val feedCache: CacheModule[String, ResponseArticleWithCount] = new CacheModule[String, ResponseArticleWithCount](feedCaffeinCache)
  val feedService: FeedService = new FeedService(feedCache, articleService)

  val cacheService: CacheService = new CacheService(
    sitemapService,
    contentTypeService,
    feedService
  )
}
