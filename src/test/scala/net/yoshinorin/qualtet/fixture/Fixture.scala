package net.yoshinorin.qualtet.fixture

import org.http4s.Uri
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.{Cache => CaffeineCache}
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.archives._
import net.yoshinorin.qualtet.domains.articles._
import net.yoshinorin.qualtet.domains.authors._
import net.yoshinorin.qualtet.domains.contents._
import net.yoshinorin.qualtet.domains.contentTypes._
import net.yoshinorin.qualtet.domains.externalResources._
import net.yoshinorin.qualtet.domains.robots._
import net.yoshinorin.qualtet.domains.sitemaps.{SitemapsRepositoryDoobieImpl, SitemapService, Url}
import net.yoshinorin.qualtet.domains.tags._
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
import java.util.concurrent.TimeUnit
import java.util.Locale
import wvlet.airframe.ulid.ULID
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed
import net.yoshinorin.qualtet.http.routes.CacheRoute
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount
import net.yoshinorin.qualtet.Modules

// Just test data
object Fixture {

  def generateUlid(): String = {
    ULID.newULIDString.toLowerCase(Locale.ENGLISH)
  }

  val h: String = Config.httpHost
  val p: String = Config.httpPort.toString()
  val host = Uri.unsafeFromString(s"http://${h}:${p}")

  // TODO: from config for cache options
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache = new CacheModule[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService: ContentTypeService = new ContentTypeService(Modules.contentTypeRepository, contentTypeCache)(Modules.doobieContext)

  val sitemapRepository: SitemapsRepositoryDoobieImpl = new SitemapsRepositoryDoobieImpl()
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache = new CacheModule[String, Seq[Url]](sitemapCaffeinCache)
  val sitemapService: SitemapService = new SitemapService(sitemapRepository, sitemapCache)(Modules.doobieContext)

  val feedCaffeinCache: CaffeineCache[String, ResponseArticleWithCount] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ResponseArticleWithCount]
  val feedCache: CacheModule[String, ResponseArticleWithCount] = new CacheModule[String, ResponseArticleWithCount](feedCaffeinCache)
  val feedService: FeedService = new FeedService(feedCache, Modules.articleService)

  val authProvider: AuthProvider = new AuthProvider(Modules.authService)

  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val archiveRoute: ArchiveRoute = new ArchiveRoute(Modules.archiveService)
  val articleRoute: ArticleRoute = new ArticleRoute(Modules.articleService)
  val authorRoute: AuthorRoute = new AuthorRoute(Modules.authorService)
  val authRoute: AuthRoute = new AuthRoute(Modules.authService)
  val cacheRoute: CacheRoute = new CacheRoute(Modules.cacheService)
  val contentTypeRoute: ContentTypeRoute = new ContentTypeRoute(Modules.contentTypeService)
  val contentRoute: ContentRoute = new ContentRoute(Modules.contentService)
  val feedRoute: FeedRoute = new FeedRoute(Modules.feedService)
  val homeRoute: HomeRoute = new HomeRoute()
  val sitemapRoute: SitemapRoute = new SitemapRoute(Modules.sitemapService)
  val tagRoute: TagRoute = new TagRoute(Modules.tagService, Modules.articleService)

  val router = new net.yoshinorin.qualtet.http.Router(
    authProvider,
    apiStatusRoute,
    archiveRoute,
    articleRoute,
    authorRoute,
    authRoute,
    cacheRoute,
    contentRoute,
    contentTypeRoute,
    feedRoute,
    homeRoute,
    sitemapRoute,
    tagRoute
  )

  def makeRouter(
    authProvider: AuthProvider = authProvider,
    apiStatusRoute: ApiStatusRoute = apiStatusRoute,
    archiveRoute: ArchiveRoute = archiveRoute,
    articleRoute: ArticleRoute = articleRoute,
    authorRoute: AuthorRoute = authorRoute,
    authRoute: AuthRoute = authRoute,
    cacheRoute: CacheRoute = cacheRoute,
    contentRoute: ContentRoute = contentRoute,
    contentTypeRoute: ContentTypeRoute = contentTypeRoute,
    feedRoute: FeedRoute = feedRoute,
    homeRoute: HomeRoute = homeRoute,
    sitemapRoute: SitemapRoute = sitemapRoute,
    tagRoute: TagRoute = tagRoute
  ) = new net.yoshinorin.qualtet.http.Router(
    authProvider = authProvider,
    apiStatusRoute = apiStatusRoute,
    archiveRoute = archiveRoute,
    articleRoute = articleRoute,
    authorRoute = authorRoute,
    authRoute = authRoute,
    cacheRoute = cacheRoute,
    contentRoute = contentRoute,
    contentTypeRoute = contentTypeRoute,
    feedRoute = feedRoute,
    homeRoute = homeRoute,
    sitemapRoute = sitemapRoute,
    tagRoute = tagRoute
  )

  val authorId: AuthorId = AuthorId("01febb8az5t42m2h68xj8c754a")
  val authorId2: AuthorId = AuthorId("01febb8az5t42m2h68xj8c754b")
  // NOTE: `pass`
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

  val nonExistsUserToken =
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDEiLCJzdWIiOiIwMWZ5NGVoMWtrMjJ5NnMxemI4YW1yeXhzcCIsImF1ZCI6InF1YWx0ZXRfZGV2XzExMTEiLCJleHAiOjE2NDcyNzMwMTYsImlhdCI6MTY0NzI2OTQxNiwianRpIjoiMDFmeTRlaDN6enhyaGM5NW1yN3NoMmJrc2IifQ.Y5lGsmXoyQR4fsI6QSceufXBmVNSHEJCSut5CVew4KQa9HnN9IaRuTwSvlrM6XV42FrutPVRIVJGqSwZBkvpuWtRZTQ42kcsURuk6Bhoo\nRqkaLuyqPGCeDbnqdWHIAJ8TW7hoVPpx2i0Wfg6d3mUs3EtaMWQ4AV_dmcnOwfhpR6YGTmNAzQNG7Z_CnlWh-VCDmSQdzd8a_BwFgcjNZPKfoTjwZ0V2F2ORWQlVf1z9Gg6s7dIm02mpKp38c2VhqtLNl7SV-F1HibxRDW5-MKeBUmb7Neb9Y9THJ1qoAY93NkYYuCru9_h__-kpWwMvKnRaKHgdZV63p2v5Uj1tMKvdw"

  // JhonDue2's expire token
  val expiredToken =
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwMDEiLCJzdWIiOiIwMWZ5NGNubm5oODh4ZGczcTE3dzlxbjcyeCIsImF1ZCI6InF1YWx0ZXRfZGV2XzExMTEiLCJleHAiOjE2NDcyNzE3MjMsImlhdCI6MTY0NzI2ODEyMywianRpIjoiMDFmeTRkOW15bTVxdjQ3ZzRlZTd0NWR2bmYifQ.01nf-DMdqdMvrCItuZdV5awS2VJYiEhbtlSv-mSe8VmJkBDJqeySlce_gXmuMzIWRYusHXOQpgmtqp0XauDW_yNFstic8z-sFZfVRKu59\nYO_VKWOjq_W1vCFTHniAT6z8LGWwZuYZ-0rYHh2VzykGPzrWO2RSfSR5QetFW6sYP22TgE6FK8C-ZAcCy4V9ZMkg14jAmsTkE6LcemqGudgruEcV2IHuAwINe5_UQCLvL2Coel8TjSWgjpIKw48Odv0UpPeUjctEbfNp84YI0Znp1IHIElGVrdT6dLnHEqPw1i3AVGxVPJsg1AGbsH0UaDXLyr_xt9iyZ9zMp9e4Pkwhw"

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
    htmlContent = "this is a html content",
    robotsAttributes = Attributes("noarchive, noimageindex"),
    tags = List("Scala", "http4s"),
    externalResources = List(
      ExternalResources(
        ExternalResourceKind("js"),
        values = List("test", "foo", "bar")
      )
    )
  )

  val requestContentNoMetas: RequestContent = RequestContent(
    contentType = "article",
    path = Path("/test/no-metas"),
    title = "this is a title",
    rawContent = "this is a raw content",
    htmlContent = "this is a html content",
    robotsAttributes = Attributes("noarchive, noimageindex"),
    tags = List(),
    externalResources = List()
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
    id = ContentId(generateUlid()),
    path = Path("/test"),
    title = "title",
    content = "this is a content",
    publishedAt = 0,
    updatedAt = 0
  )

  val responseArticle2: ResponseArticle = ResponseArticle(
    id = ContentId(generateUlid()),
    path = Path("/test/path2"),
    title = "title2",
    content = "this is a content2",
    publishedAt = 1,
    updatedAt = 2
  )

  val responseArticleWithLongLengthContent: ResponseArticle = ResponseArticle(
    id = ContentId(generateUlid()),
    path = Path("/test"),
    title = "title",
    content = "a" * 101,
    publishedAt = 0,
    updatedAt = 0
  )

  val feed1: ResponseFeed = ResponseFeed(
    title = "feedTitle1",
    link = Path("/feed1"),
    id = Path("/feed1"),
    published = 0,
    updated = 0
  )

  val feed2: ResponseFeed = ResponseFeed(
    title = "feedTitle2",
    link = Path("/feed2"),
    id = Path("/feed2"),
    published = 0,
    updated = 0
  )
}
