package net.yoshinorin.qualtet.config

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.config.ConfigSpec
class ConfigSpec extends AnyWordSpec {

  "Config" should {
    "database configuration gettable from application.conf" in {
      assert(Config.dbUrl == "jdbc:mariadb://127.0.0.1/qualtet?useUnicode=true&characterEncoding=utf8mb4")
      assert(Config.dbUser == "root")
      assert(Config.dbPassword == "pass")
    }

    "http server configuration gettable from application.conf" in {
      assert(Config.httpHost == "0.0.0.0")
      assert(Config.httpPort == 9001)
    }

    "jwt configuration gettable from application.conf" in {
      assert(Config.jwtIss == "http://localhost:9001")
      assert(Config.jwtAud == "qualtet_dev_1111")
    }
  }

}
