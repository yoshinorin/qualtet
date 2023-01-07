package net.yoshinorin.qualtet.config

import net.yoshinorin.qualtet.Modules._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.config.ConfigSpec
class ConfigSpec extends AnyWordSpec {

  "Config" should {
    "database configuration gettable from application.conf" in {
      assert(config.db.url === "jdbc:mariadb://127.0.0.1:33066/qualtet?useUnicode=true&characterEncoding=utf8mb4")
      assert(config.db.user === "root")
      assert(config.db.password === "pass")
    }

    "http server configuration gettable from application.conf" in {
      assert(config.http.host === "0.0.0.0")
      assert(config.http.port === 9001)
    }

    "jwt configuration gettable from application.conf" in {
      assert(config.jwt.iss === "http://localhost:9001")
      assert(config.jwt.aud === "qualtet_dev_1111")
      assert(config.jwt.expiration === 3600)
    }

    "cache configuration gettable from application.conf" in {
      assert(config.cache.contentType === 604800)
      assert(config.cache.sitemap === 3601)
      assert(config.cache.feed === 7200)
    }
  }

}
