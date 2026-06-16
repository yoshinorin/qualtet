package net.yoshinorin.qualtet.config

import com.typesafe.config.{Config as TypeSafeConfig, ConfigFactory}
import scala.jdk.CollectionConverters.*

import java.nio.file.{Path, Paths}
import java.util.ArrayList

final case class DBConfig(url: String, user: String, password: String, connectionPool: DBConnectionPool)
final case class DBConnectionPool(maxLifetime: Long, maximumPoolSize: Int)
final case class HttpConfig(host: String, port: Int, endpoints: HttpEndpointsConfig)
final case class HttpEndpointsConfig(system: HttpSystemEndpointConfig)
final case class HttpSystemEndpointMetadata(enabled: Boolean)
final case class HttpSystemEndpointConfig(metadata: HttpSystemEndpointMetadata)
final case class CorsConfig(allowOrigins: List[String])
final case class JwtConfig(iss: String, aud: String, expiration: Long)
final case class CacheConfig(contentType: Long, sitemap: Long, feed: Long, tags: Long)
final case class FeedConfig(limit: Int)
final case class SearchConfig(maxWords: Int, minWordLength: Int, maxWordLength: Int)
final case class OtelServiceConfig(name: Option[String], namespace: Option[String])
final case class OtelExporterConfig(endpoint: Option[String], headers: Option[String], protocol: Option[String])
final case class OtelConfig(enabled: Option[Boolean], service: OtelServiceConfig, exporter: OtelExporterConfig, propagator: Option[String])

sealed trait KeyPairSourceConfig
object KeyPairSourceConfig {
  final case class InMemory(algorithm: String, length: Int, secureRandomAlgorithm: Option[String]) extends KeyPairSourceConfig
  final case class File(algorithm: String, publicKeyPath: Path, privateKeyPath: Path) extends KeyPairSourceConfig
  final case class Pem(algorithm: String, publicKeyPem: String, privateKeyPem: String) extends KeyPairSourceConfig
}

final case class ApplicationConfig(
  db: DBConfig,
  http: HttpConfig,
  cors: CorsConfig,
  jwt: JwtConfig,
  cache: CacheConfig,
  feed: FeedConfig,
  search: SearchConfig,
  otel: OtelConfig,
  keypair: KeyPairSourceConfig
)

object ApplicationConfig {

  private val config: TypeSafeConfig = ConfigFactory.load

  private val dbUrl: String = config.getString("db.dataSource.url")
  private val dbUser: String = config.getString("db.dataSource.user")
  private val dbPassword: String = config.getString("db.dataSource.password")
  private val dbConnectionPool: DBConnectionPool =
    DBConnectionPool(maxLifetime = config.getLong("db.connection-pool.maxLifetime"), maximumPoolSize = config.getInt("db.connection-pool.maximumPoolSize"))

  private val httpHost: String = config.getString("http.host")
  private val httpPort: Int = config.getInt("http.port")
  private val httpEndpoints: HttpEndpointsConfig =
    HttpEndpointsConfig(system = HttpSystemEndpointConfig(metadata = HttpSystemEndpointMetadata(config.getBoolean("http.endpoints.system.metadata.enabled"))))

  private val corsAllowOrigins: List[String] = config.getList("cors.allow-origins").unwrapped().asInstanceOf[ArrayList[String]].asScala.toList

  private val jwtIss: String = config.getString("jwt.iss")
  private val jwtAud: String = config.getString("jwt.aud")
  private val jwtExpiration: Long = config.getLong("jwt.expiration")

  private val cacheContentType: Long = config.getLong("cache.content-type")
  private val cacheSitemap: Long = config.getLong("cache.sitemap")
  private val cacheFeed: Long = config.getLong("cache.feed")
  private val cacheTags: Long = config.getLong("cache.tags")

  private val feedLimit: Int = config.getInt("feed.limit")

  private val searchMaxWords: Int = config.getInt("search.max-words")
  private val searchMinWordLength: Int = config.getInt("search.min-word-length")
  private val searchMaxWordLength: Int = config.getInt("search.max-word-length")

  private def getOptionalBoolean(path: String): Option[Boolean] =
    if (config.hasPath(path)) Some(config.getBoolean(path)) else None

  private def getOptionalString(path: String): Option[String] =
    if (config.hasPath(path)) Some(config.getString(path)) else None

  private val otelEnabled: Option[Boolean] = getOptionalBoolean("otel.enabled")
  private val otelServiceName: Option[String] = getOptionalString("otel.service.name")
  private val otelServiceNamespace: Option[String] = getOptionalString("otel.service.namespace")
  private val otelExporterEndpoint: Option[String] = getOptionalString("otel.exporter.endpoint")
  private val otelExporterHeaders: Option[String] = getOptionalString("otel.exporter.headers")
  private val otelExporterProtocol: Option[String] = getOptionalString("otel.exporter.protocol")
  private val otelPropagator: Option[String] = getOptionalString("otel.propagator")

  private def loadKeyPair: KeyPairSourceConfig = {
    val source = if (config.hasPath("keypair.source")) config.getString("keypair.source") else "IN-MEMORY"
    val algorithm = if (config.hasPath("keypair.algorithm")) config.getString("keypair.algorithm") else "RSA"

    source match {
      case "IN-MEMORY" =>
        val length = if (config.hasPath("keypair.length")) config.getInt("keypair.length") else 2048
        KeyPairSourceConfig.InMemory(algorithm, length, getOptionalString("keypair.secure-random"))
      case "FILE" =>
        KeyPairSourceConfig.File(
          algorithm,
          Paths.get(config.getString("keypair.public-key-path")),
          Paths.get(config.getString("keypair.private-key-path"))
        )
      case "PEM" =>
        KeyPairSourceConfig.Pem(
          algorithm,
          config.getString("keypair.public-key-pem"),
          config.getString("keypair.private-key-pem")
        )
      case other =>
        throw new IllegalArgumentException(s"unknown keypair.source: '$other' (expected: 'IN-MEMORY' | 'FILE' | 'PEM')")
    }
  }

  def load: ApplicationConfig = ApplicationConfig(
    db = DBConfig(dbUrl, dbUser, dbPassword, dbConnectionPool),
    http = HttpConfig(httpHost, httpPort, httpEndpoints),
    cors = CorsConfig(corsAllowOrigins),
    jwt = JwtConfig(jwtIss, jwtAud, jwtExpiration),
    cache = CacheConfig(cacheContentType, cacheSitemap, cacheFeed, cacheTags),
    feed = FeedConfig(feedLimit),
    search = SearchConfig(searchMaxWords, searchMinWordLength, searchMaxWordLength),
    otel = OtelConfig(
      enabled = otelEnabled,
      service = OtelServiceConfig(otelServiceName, otelServiceNamespace),
      exporter = OtelExporterConfig(otelExporterEndpoint, otelExporterHeaders, otelExporterProtocol),
      propagator = otelPropagator
    ),
    keypair = loadKeyPair
  )

}
