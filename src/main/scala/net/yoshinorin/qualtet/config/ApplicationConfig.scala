package net.yoshinorin.qualtet.config

import com.typesafe.config.{Config => TypeSafeConfig, ConfigFactory}
import com.typesafe.config.ConfigList
import scala.jdk.CollectionConverters._

import java.util.ArrayList

final case class DBConfig(url: String, user: String, password: String)
final case class HttpConfig(host: String, port: Int)
final case class CorsConfig(allowOrigins: List[String])
final case class JwtConfig(iss: String, aud: String, expiration: Long)
final case class CacheConfig(contentType: Long, sitemap: Long, feed: Long)
final case class SearchConfig(maxWords: Int, minWordLength: Int, maxWordLength: Int)
final case class ApplicationConfig(
  db: DBConfig,
  http: HttpConfig,
  cors: CorsConfig,
  jwt: JwtConfig,
  cache: CacheConfig,
  search: SearchConfig
)

object ApplicationConfig {

  private[this] val config: TypeSafeConfig = ConfigFactory.load

  private val dbUrl: String = config.getString("db.ctx.dataSource.url")
  private val dbUser: String = config.getString("db.ctx.dataSource.user")
  private val dbPassword: String = config.getString("db.ctx.dataSource.password")

  private val httpHost: String = config.getString("http.host")
  private val httpPort: Int = config.getInt("http.port")

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private val corsAllowOrigins: List[String] = config.getList("cors.allow-origins").unwrapped().asInstanceOf[ArrayList[String]].asScala.toList

  private val jwtIss: String = config.getString("jwt.iss")
  private val jwtAud: String = config.getString("jwt.aud")
  private val jwtExpiration: Long = config.getLong("jwt.expiration")

  private val cacheContentType: Long = config.getLong("cache.content-type")
  private val cacheSitemap: Long = config.getLong("cache.sitemap")
  private val cacheFeed: Long = config.getLong("cache.feed")

  private val searchMaxWords: Int = config.getInt("search.max-words")
  private val searchMinWordLength: Int = config.getInt("search.min-word-length")
  private val searchMaxWordLength: Int = config.getInt("search.max-word-length")

  def load: ApplicationConfig = ApplicationConfig(
    db = DBConfig(dbUrl, dbUser, dbPassword),
    http = HttpConfig(httpHost, httpPort),
    cors = CorsConfig(corsAllowOrigins),
    jwt = JwtConfig(jwtIss, jwtAud, jwtExpiration),
    cache = CacheConfig(cacheContentType, cacheSitemap, cacheFeed),
    search = SearchConfig(searchMaxWords, searchMinWordLength, searchMaxWordLength)
  )

}
