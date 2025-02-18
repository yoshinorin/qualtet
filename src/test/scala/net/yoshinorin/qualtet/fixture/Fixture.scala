package net.yoshinorin.qualtet.fixture

import cats.Monad
import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import org.http4s.Uri
import org.http4s.Response
import org.typelevel.log4cats.{LoggerFactory => Log4CatsLoggerFactory}
import org.typelevel.log4cats.slf4j.{Slf4jFactory => Log4CatsSlf4jFactory}
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.{Cache => CaffeineCache}
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import net.yoshinorin.qualtet.config.ApplicationConfig
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.http.CorsProvider
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.articles.*
import net.yoshinorin.qualtet.domains.authors.*
import net.yoshinorin.qualtet.domains.contents.*
import net.yoshinorin.qualtet.domains.contentTypes.*
import net.yoshinorin.qualtet.domains.robots.*
import net.yoshinorin.qualtet.domains.sitemaps.{SitemapService, SitemapsRepository, Url}
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
import java.util.concurrent.TimeUnit
import wvlet.airframe.ulid.ULID
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.domains.articles.ArticleWithCountResponseModel
import net.yoshinorin.qualtet.domains.tags.{TagResponseModel, TagService}
import net.yoshinorin.qualtet.Modules
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import cats.effect.unsafe.implicits.global
import net.yoshinorin.qualtet.domains.series.{SeriesName, SeriesRequestModel}
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources

// Just test data
object Fixture {

  def generateUlid(): String = {
    ULID.newULIDString.toLower
  }

  val config = ApplicationConfig.load
  val h: String = config.http.host
  val p: String = config.http.port.toString()
  val host = Uri.unsafeFromString(s"http://${h}:${p}")

  val fixtureTx = Transactor.fromDriverManager[IO](
    driver = "org.mariadb.jdbc.Driver",
    url = config.db.url,
    user = config.db.user,
    password = config.db.password,
    logHandler = None
  )
  private val modules = Modules(fixtureTx)
  given log4catsLogger: Log4CatsLoggerFactory[IO] = Log4CatsSlf4jFactory.create[IO]
  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(fixtureTx)

  val migrator = modules.migrator
  val articleService = modules.articleService
  val archiveService = modules.archiveService
  val authorService = modules.authorService
  val authService = modules.authService
  val cacheService = modules.cacheService
  val contentService = modules.contentService
  val contentTaggingService = modules.contentTaggingService
  val externalResourceService = modules.externalResourceService
  val seriesService = modules.seriesService
  val searchService = modules.searchService

  // TODO: from config for cache options
  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ContentType]
  val contentTypeCache = new CacheModule[String, ContentType](contentTypeCaffeinCache)
  val contentTypeService = new ContentTypeService(modules.contentTypeRepository, contentTypeCache)

  val sitemapRepository: SitemapsRepository[ConnectionIO] = summon[SitemapsRepository[ConnectionIO]]
  val sitemapCaffeinCache: CaffeineCache[String, Seq[Url]] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, Seq[Url]]
  val sitemapCache = new CacheModule[String, Seq[Url]](sitemapCaffeinCache)
  val sitemapService = new SitemapService(sitemapRepository, sitemapCache)

  val feedCaffeinCache: CaffeineCache[String, ArticleWithCountResponseModel] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ArticleWithCountResponseModel]
  val feedCache: CacheModule[String, ArticleWithCountResponseModel] = new CacheModule[String, ArticleWithCountResponseModel](feedCaffeinCache)
  val feedService = new FeedService(modules.feedsPagination, feedCache, modules.articleService)

  val tagsCaffeinCache: CaffeineCache[String, Seq[TagResponseModel]] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, Seq[TagResponseModel]]
  val tagsCache: CacheModule[String, Seq[TagResponseModel]] = new CacheModule[String, Seq[TagResponseModel]](tagsCaffeinCache)
  val tagService = new TagService(modules.tagRepository, tagsCache, modules.contentTaggingService)

  val authProvider = new AuthProvider(modules.authService)
  val corsProvider = new CorsProvider(modules.config.cors)

  val archiveRouteV1 = new ArchiveRouteV1(modules.archiveService)
  val articleRouteV1 = new ArticleRouteV1(modules.articleService)
  val authorRouteV1 = new AuthorRouteV1(modules.authorService)
  val authRouteV1 = new AuthRouteV1(modules.authService)
  val cacheRouteV1 = new CacheRouteV1(authProvider, modules.cacheService)
  val contentTypeRouteV1 = new ContentTypeRouteV1(modules.contentTypeService)
  val contentRouteV1 = new ContentRouteV1(authProvider, modules.contentService)
  val feedRouteV1 = new FeedRouteV1(modules.feedService)
  val homeRoute: HomeRoute = new HomeRoute()
  val searchRouteV1 = new SearchRouteV1(modules.searchService)
  val seriesRouteV1 = new SeriesRouteV1(authProvider, modules.seriesService)
  val sitemapRouteV1 = new SitemapRouteV1(modules.sitemapService)
  val systemRouteV1 = new SystemRouteV1(modules.config.http.endpoints.system)
  val tagRouteV1 = new TagRouteV1(authProvider, tagService, modules.articleService)

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

  def makeRouter[F[_]: Monad](
    archiveRouteV1: ArchiveRouteV1[F] = archiveRouteV1,
    articleRouteV1: ArticleRouteV1[F] = articleRouteV1,
    authorRouteV1: AuthorRouteV1[F] = authorRouteV1,
    authRouteV1: AuthRouteV1[F] = authRouteV1,
    cacheRouteV1: CacheRouteV1[F] = cacheRouteV1,
    contentRouteV1: ContentRouteV1[F] = contentRouteV1,
    contentTypeRouteV1: ContentTypeRouteV1[F] = contentTypeRouteV1,
    feedRouteV1: FeedRouteV1[F] = feedRouteV1,
    homeRoute: HomeRoute = homeRoute,
    searchRouteV1: SearchRouteV1[F] = searchRouteV1,
    seriesRouteV1: SeriesRouteV1[F] = seriesRouteV1,
    sitemapRouteV1: SitemapRouteV1[F] = sitemapRouteV1,
    tagRouteV1: TagRouteV1[F] = tagRouteV1,
    systemRouteV1: SystemRouteV1 = systemRouteV1
  ) = new net.yoshinorin.qualtet.http.Router(
    corsProvider = corsProvider,
    archiveRouteV1 = archiveRouteV1,
    articleRouteV1 = articleRouteV1,
    authorRouteV1 = authorRouteV1,
    authRouteV1 = authRouteV1,
    cacheRouteV1 = cacheRouteV1,
    contentRouteV1 = contentRouteV1,
    contentTypeRouteV1 = contentTypeRouteV1,
    feedRouteV1 = feedRouteV1,
    homeRoute = homeRoute,
    searchRouteV1 = searchRouteV1,
    seriesRouteV1 = seriesRouteV1,
    sitemapRouteV1 = sitemapRouteV1,
    systemRouteV1 = systemRouteV1,
    tagRouteV1 = tagRouteV1
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
  val fullRobotsAttributes: Attributes = Attributes("all, noindex, nofollow, none, noarchive, nosnippet, notranslate, noimageindex")

  def createContentRequestModels(
    numberOfCreateContents: Int,
    specName: String,
    series: Option[SeriesName] = None,
    externalResources: List[ExternalResources] = List()
  ): List[ContentRequestModel] = {
    (0 until numberOfCreateContents).toList
      .map(_.toString())
      .map(i =>
        ContentRequestModel(
          contentType = "article",
          path = Path(s"/test/${specName}-${i}"),
          title = s"this is a ${specName} title ${i}",
          rawContent = s"this is a ${specName} raw content ${i}",
          htmlContent = s"this is a ${specName} html content ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(s"${specName}Tag${i}"),
          series = series,
          externalResources = externalResources
        )
      )
  }

  extension (requestContents: List[ContentRequestModel]) {
    def unsafeCreateConternt() = {
      requestContents.foreach { rc =>
        modules.contentService.create(AuthorName(author.name.value), rc).unsafeRunSync()
      }
    }
  }

  extension (requestSeries: List[SeriesRequestModel]) {
    def unsafeCreateSeries() = {
      requestSeries.foreach { rs =>
        modules.seriesService.create(rs).unsafeRunSync()
      }
    }
  }

  def unsafeDecode[T](response: Response[IO]): JsonValueCodec[T] ?=> T = {
    response.as[String].unsafeRunSync().decode
  }

  extension (s: String) {
    def replaceNewlineAndSpace: String = s.replaceAll("\n", "").replaceAll(" ", "")
  }
}
