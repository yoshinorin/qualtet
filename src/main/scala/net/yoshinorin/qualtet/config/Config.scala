package net.yoshinorin.qualtet.config

import com.typesafe.config.{Config => TypeSafeConfig, ConfigFactory}

object Config {

  private[this] val config: TypeSafeConfig = ConfigFactory.load

  val dbUrl: String = config.getString("db.ctx.dataSource.url")
  val dbUser: String = config.getString("db.ctx.dataSource.user")
  val dbPassword: String = config.getString("db.ctx.dataSource.password")

  val httpHost: String = config.getString("http.host")
  val httpPort: Int = config.getInt("http.port")

  val jwtIss: String = config.getString("jwt.iss")
  val jwtAud: String = config.getString("jwt.aud")
  val jwtExpiration: Long = config.getLong("jwt.expiration")

  val cacheContentType: Long = config.getLong("cache.content-type")
  val cacheSitemap: Long = config.getLong("cache.sitemap")
  val cacheFeed: Long = config.getLong("cache.feed")

}
