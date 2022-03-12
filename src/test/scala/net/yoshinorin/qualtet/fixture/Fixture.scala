package net.yoshinorin.qualtet.fixture

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.{Cache => CaffeineCache}
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.domains.models.archives.{DoobieArchiveRepository, ResponseArchive}
import net.yoshinorin.qualtet.domains.models.articles.{DoobieArticleRepository, ResponseArticle}
import net.yoshinorin.qualtet.domains.models.authors._
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, ContentTypeId, DoobieContentTypeRepository}
import net.yoshinorin.qualtet.domains.models.contents.{ContentId, DoobieContentRepository, DoobieContentTaggingRepository, Path, RequestContent}
import net.yoshinorin.qualtet.domains.models.externalResources.{DoobieExternalResourceRepository, ExternalResourceKind, ExternalResources}
import net.yoshinorin.qualtet.domains.models.robots.{Attributes, DoobieRobotsRepository}
import net.yoshinorin.qualtet.domains.models.sitemaps.{DoobieSitemapsRepository, Url}
import net.yoshinorin.qualtet.domains.models.tags.{DoobieTagRepository, TagId}
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
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.stub.DoobieStubContext
import net.yoshinorin.qualtet.utils.Cache
import pdi.jwt.JwtAlgorithm

import java.security.SecureRandom
import java.util.concurrent.TimeUnit

// Just test data
object Fixture {

  val doobieContext = new DoobieContext()
  val doobieStubContext = new DoobieStubContext()

  // TODO: repositories, services, routes are just copy from bootstrap. Should DRY.
  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes
  val signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance = new Jwt(JwtAlgorithm.RS256, keyPair, signature)

  val authorRepository = new DoobieAuthorRepository
  val authorService: AuthorService = new AuthorService(authorRepository)(doobieContext)

  val authService = new AuthService(authorService, jwtInstance)

  val contentTypeRepository = new DoobieContentTypeRepository
  // TODO: from config for cache options
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache = new Cache[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService: ContentTypeService = new ContentTypeService(contentTypeRepository, contentTypeCache)(doobieContext)

  val tagRepository = new DoobieTagRepository
  val tagService = new TagService(tagRepository)(doobieContext)

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
    )(doobieContext)

  val articleRepository = new DoobieArticleRepository
  val articleService: ArticleService = new ArticleService(articleRepository, contentTypeService)(doobieContext)

  val archiveRepository = new DoobieArchiveRepository
  val archiveService: ArchiveService = new ArchiveService(archiveRepository, contentTypeService)(doobieContext)

  val sitemapRepository = new DoobieSitemapsRepository
  // TODO: from inf cache
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache = new Cache[String, Seq[Url]](sitemapCaffeinCache)
  val sitemapService = new SitemapService(sitemapRepository, sitemapCache)(doobieContext)

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

  val authorId: AuthorId = AuthorId("01febb8az5t42m2h68xj8c754a")
  val authorId2: AuthorId = AuthorId("01febb8az5t42m2h68xj8c754b")
  val validBCryptPassword: BCryptPassword = BCryptPassword("$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O")

  val author: Author = Author(
    id = authorId,
    name = AuthorName("JhonDue"),
    displayName = AuthorDisplayName("JD"),
    password = validBCryptPassword
  )

  val author2: Author = Author(
    id = authorId2,
    name = AuthorName("JhonDue2"),
    displayName = AuthorDisplayName("JD2"),
    password = validBCryptPassword
  )

  val contentId: ContentId = ContentId("01febb1333pd3431q1a1e00fbt")
  val contentTypeId: ContentTypeId = ContentTypeId("01febb1333pd3431q1a1e01fbc")
  val articleContentType: ContentType = ContentType(contentTypeId, "articles")

  val tagId: TagId = TagId("01frdbe1g83533h92rkhy8ctkw")

  val fullRobotsAttributes: Attributes = Attributes("all, noindex, nofollow, none, noarchive, nosnippet, notranslate, noimageindex")

  val requestContent1: RequestContent = RequestContent(
    contentType = "article",
    path = Path("/test/path"),
    title = "this is a title",
    rawContent = "this is a raw content",
    robotsAttributes = Attributes("noarchive, noimageindex"),
    tags = Option(List("Scala", "Akka")),
    externalResources = Option(
      List(
        ExternalResources(
          ExternalResourceKind("js"),
          values = List("test", "foo", "bar")
        )
      )
    )
  )

  val requestContentNoMetas: RequestContent = RequestContent(
    contentType = "article",
    path = Path("/test/no-metas"),
    title = "this is a title",
    rawContent = "",
    htmlContent = Option("this is a html content"),
    robotsAttributes = Attributes("noarchive, noimageindex"),
    tags = Option(List()),
    externalResources = Option(List())
  )

  val responseArchive: ResponseArchive = ResponseArchive(
    path = Path("/test"),
    title = "title",
    publishedAt = 1567814290
  )

  val responseArchive2: ResponseArchive =
    ResponseArchive(
      path = Path("/test/path1"),
      title = "title1",
      publishedAt = 1567814290
    )

  val responseArchive3: ResponseArchive =
    ResponseArchive(
      path = Path("/test/path2"),
      title = "title2",
      publishedAt = 1567814391
    )

  val responseArticle1: ResponseArticle = ResponseArticle(
    path = Path("/test"),
    title = "title",
    content = "this is a content",
    publishedAt = 0,
    updatedAt = 0
  )

  val responseArticle2: ResponseArticle = ResponseArticle(
    path = Path("/test/path2"),
    title = "title2",
    content = "this is a content2",
    publishedAt = 1,
    updatedAt = 2
  )

  val responseArticleWithLongLengthContent: ResponseArticle = ResponseArticle(
    path = Path("/test"),
    title = "title",
    content = "a" * 101,
    publishedAt = 0,
    updatedAt = 0
  )
}
