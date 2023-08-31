package net.yoshinorin.qualtet.http

import org.http4s.Uri
import org.http4s.headers.Origin
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.config.CorsConfig
import java.net.MalformedURLException

// testOnly net.yoshinorin.qualtet.http.CorsProviderSpec
class CorsProviderSpec extends AnyWordSpec {

  import net.yoshinorin.qualtet.fixture.Fixture.log4catsLogger

  "CorsProvider" should {
    "returns origins by config" in {
      val corsProvider = new CorsProvider(corsConfig =
        CorsConfig(allowOrigins =
          List(
            "http://example.com:8080",
            "http://localhost:8080",
            "http://localhost:8080",
            "http://localhost:8081",
            "https://example.net:8081"
          )
        )
      )
      assert(
        corsProvider.origins === Set(
          Origin.Host(Uri.Scheme.http, Uri.RegName("example.com"), Some(8080)),
          Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), Some(8080)),
          Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), Some(8081)),
          Origin.Host(Uri.Scheme.https, Uri.RegName("example.net"), Some(8081))
        )
      )
    }

    "returns default origin if config is empty" in {
      val corsProvider = new CorsProvider(corsConfig = CorsConfig(allowOrigins = List()))
      assert(
        corsProvider.origins === Set()
      )
    }

    "thrown java.net.MalformedURLException if configs contains invalid URL." in {
      assertThrows[MalformedURLException] {
        new CorsProvider(corsConfig =
          CorsConfig(allowOrigins =
            List(
              "http://example.com:8080",
              "http://localhost:8080",
              "http://invalid:invalid",
              "http://example.com:8080",
              "http://localhost:8080"
            )
          )
        )
      }
    }
  }
}
