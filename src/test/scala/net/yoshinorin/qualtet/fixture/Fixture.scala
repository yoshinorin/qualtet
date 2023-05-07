package net.yoshinorin.qualtet.fixture

import cats.Monad
import org.http4s.Uri
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.{Cache => CaffeineCache}
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.http.CorsProvider
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.archives._
import net.yoshinorin.qualtet.domains.articles._
import net.yoshinorin.qualtet.domains.authors._
import net.yoshinorin.qualtet.domains.contents._
import net.yoshinorin.qualtet.domains.contentTypes._
import net.yoshinorin.qualtet.domains.externalResources._
import net.yoshinorin.qualtet.domains.robots._
import net.yoshinorin.qualtet.domains.sitemaps.{SitemapsRepositoryDoobieInterpreter, SitemapService, Url}
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
  SearchRoute,
  SeriesRoute,
  SitemapRoute,
  TagRoute
}
import java.util.concurrent.TimeUnit
import wvlet.airframe.ulid.ULID
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed
import net.yoshinorin.qualtet.http.routes.CacheRoute
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount
import net.yoshinorin.qualtet.Modules
import net.yoshinorin.qualtet.syntax._
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import cats.effect.unsafe.implicits.global

// Just test data
object Fixture {

  def generateUlid(): String = {
    ULID.newULIDString.toLower
  }

  val h: String = Modules.config.http.host
  val p: String = Modules.config.http.port.toString()
  val host = Uri.unsafeFromString(s"http://${h}:${p}")

  given dbContext: DoobieContext = new DoobieContext(Modules.config.db)

  // TODO: from config for cache options
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache = new CacheModule[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService = new ContentTypeService(Modules.contentTypeRepository, contentTypeCache)

  val sitemapRepository: SitemapsRepositoryDoobieInterpreter = new SitemapsRepositoryDoobieInterpreter()
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache = new CacheModule[String, Seq[Url]](sitemapCaffeinCache)
  val sitemapService = new SitemapService(sitemapRepository, sitemapCache)

  val feedCaffeinCache: CaffeineCache[String, ResponseArticleWithCount] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ResponseArticleWithCount]
  val feedCache: CacheModule[String, ResponseArticleWithCount] = new CacheModule[String, ResponseArticleWithCount](feedCaffeinCache)
  val feedService = new FeedService(feedCache, Modules.articleService)

  val authProvider = new AuthProvider(Modules.authService)
  val corsProvider = new CorsProvider(Modules.config.cors)

  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val archiveRoute = new ArchiveRoute(Modules.archiveService)
  val articleRoute = new ArticleRoute(Modules.articleService)
  val authorRoute = new AuthorRoute(Modules.authorService)
  val authRoute = new AuthRoute(Modules.authService)
  val cacheRoute = new CacheRoute(Modules.cacheService)
  val contentTypeRoute = new ContentTypeRoute(Modules.contentTypeService)
  val contentRoute = new ContentRoute(Modules.contentService)
  val feedRoute = new FeedRoute(Modules.feedService)
  val homeRoute: HomeRoute = new HomeRoute()
  val searchRoute = new SearchRoute(Modules.searchService)
  val seriesRoute = new SeriesRoute(Modules.seriesService)
  val sitemapRoute = new SitemapRoute(Modules.sitemapService)
  val tagRoute = new TagRoute(Modules.tagService, Modules.articleService)

  val router = new net.yoshinorin.qualtet.http.Router(
    authProvider,
    corsProvider,
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
    searchRoute,
    seriesRoute,
    sitemapRoute,
    tagRoute
  )

  def makeRouter[M[_]: Monad](
    authProvider: AuthProvider[M] = authProvider,
    apiStatusRoute: ApiStatusRoute = apiStatusRoute,
    archiveRoute: ArchiveRoute[M] = archiveRoute,
    articleRoute: ArticleRoute[M] = articleRoute,
    authorRoute: AuthorRoute[M] = authorRoute,
    authRoute: AuthRoute[M] = authRoute,
    cacheRoute: CacheRoute[M] = cacheRoute,
    contentRoute: ContentRoute[M] = contentRoute,
    contentTypeRoute: ContentTypeRoute[M] = contentTypeRoute,
    feedRoute: FeedRoute[M] = feedRoute,
    homeRoute: HomeRoute = homeRoute,
    searchRoute: SearchRoute[M] = searchRoute,
    seriesRoute: SeriesRoute[M] = seriesRoute,
    sitemapRoute: SitemapRoute[M] = sitemapRoute,
    tagRoute: TagRoute[M] = tagRoute
  ) = new net.yoshinorin.qualtet.http.Router(
    authProvider = authProvider,
    corsProvider = corsProvider,
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
    searchRoute = searchRoute,
    seriesRoute = seriesRoute,
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


  def createContents(requestContents: List[RequestContent]) = {
    requestContents.foreach { rc =>
      Modules.contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync()
    }
  }

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
