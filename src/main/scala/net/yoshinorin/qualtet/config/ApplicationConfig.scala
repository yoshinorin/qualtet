package net.yoshinorin.qualtet.config

import com.typesafe.config.{Config => TypeSafeConfig, ConfigFactory}

case class DBConfig(url: String, user: String, password: String)
case class HttpConfig(host: String, port: Int)
case class JwtConfig(iss: String, aud: String, expiration: Long)
case class CacheConfig(contentType: Long, sitemap: Long, feed: Long)
case class ApplicationConfig(
  db: DBConfig,
  http: HttpConfig,
  jwt: JwtConfig,
  cache: CacheConfig
)

object ApplicationConfig {

  private[this] val config: TypeSafeConfig = ConfigFactory.load

  private val dbUrl: String = config.getString("db.ctx.dataSource.url")
  private val dbUser: String = config.getString("db.ctx.dataSource.user")
  private val dbPassword: String = config.getString("db.ctx.dataSource.password")

  private val httpHost: String = config.getString("http.host")
  private val httpPort: Int = config.getInt("http.port")

  private val jwtIss: String = config.getString("jwt.iss")
  private val jwtAud: String = config.getString("jwt.aud")
  private val jwtExpiration: Long = config.getLong("jwt.expiration")

  private val cacheContentType: Long = config.getLong("cache.content-type")
  private val cacheSitemap: Long = config.getLong("cache.sitemap")
  private val cacheFeed: Long = config.getLong("cache.feed")

  def load: ApplicationConfig = ApplicationConfig(
    db = DBConfig(dbUrl, dbUser, dbPassword),
    http = HttpConfig(httpHost, httpPort),
    jwt = JwtConfig(jwtIss, jwtAud, jwtExpiration),
    cache = CacheConfig(cacheContentType, cacheSitemap, cacheFeed)
  )

}
