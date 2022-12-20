package net.yoshinorin.qualtet

import com.github.benmanes.caffeine.cache.{Caffeine, Cache => CaffeineCache}
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.archives.{ArchiveRepositoryDoobieImple, ArchiveService}
import net.yoshinorin.qualtet.domains.articles.{ArticleRepositoryDoobieImpl, ArticleService}
import net.yoshinorin.qualtet.domains.authors.{AuthorRepositoryDoobieImpl, AuthorService}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeRepositoryDoobieImpl, ContentType, ContentTypeService}
import net.yoshinorin.qualtet.domains.contentTaggings.{ContentTaggingRepositoryDoobieImple, ContentTaggingService}
import net.yoshinorin.qualtet.domains.contents.{ContentRepositoryDoobieImpl, ContentService}
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceRepositoryDoobieImpl, ExternalResourceService}
import net.yoshinorin.qualtet.domains.robots.{RobotsRepositoryDoobieImpl, RobotsService}
import net.yoshinorin.qualtet.domains.sitemaps.{SitemapsRepositoryDoobieImpl, SitemapService, Url}
import net.yoshinorin.qualtet.domains.tags.{TagRepositoryDoobieImpl, TagService}
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

  val authorRepository: AuthorRepositoryDoobieImpl = new AuthorRepositoryDoobieImpl()
  val authorService: AuthorService = new AuthorService(authorRepository)(doobieContext)

  val authService: AuthService = new AuthService(authorService, jwtInstance)

  val contentTypeRepository: ContentTypeRepositoryDoobieImpl = new ContentTypeRepositoryDoobieImpl()
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(Config.cacheContentType, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache: CacheModule[String, ContentType] = new CacheModule[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService: ContentTypeService = new ContentTypeService(contentTypeRepository, contentTypeCache)(doobieContext)

  val robotsRepository: RobotsRepositoryDoobieImpl = new RobotsRepositoryDoobieImpl()
  val robotsService: RobotsService = new RobotsService(robotsRepository)

  val externalResourceRepository: ExternalResourceRepositoryDoobieImpl = new ExternalResourceRepositoryDoobieImpl()
  val externalResourceService: ExternalResourceService = new ExternalResourceService(externalResourceRepository)

  val contentTaggingRepository: ContentTaggingRepositoryDoobieImple = new ContentTaggingRepositoryDoobieImple()
  val contentTaggingService: ContentTaggingService = new ContentTaggingService(contentTaggingRepository)(doobieContext)

  val tagRepository: TagRepositoryDoobieImpl = new TagRepositoryDoobieImpl()
  val tagService: TagService = new TagService(tagRepository, contentTaggingService)(doobieContext)

  val contentRepository: ContentRepositoryDoobieImpl = new ContentRepositoryDoobieImpl()
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

  val articleRepository: ArticleRepositoryDoobieImpl = new ArticleRepositoryDoobieImpl()
  val articleService: ArticleService = new ArticleService(articleRepository, contentTypeService)(doobieContext)

  val archiveRepository: ArchiveRepositoryDoobieImple = new ArchiveRepositoryDoobieImple()
  val archiveService: ArchiveService = new ArchiveService(archiveRepository, contentTypeService)(doobieContext)

  val sitemapRepository: SitemapsRepositoryDoobieImpl = new SitemapsRepositoryDoobieImpl()
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
