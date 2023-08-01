package net.yoshinorin.qualtet.http

import org.http4s.Uri
import org.http4s.headers.Origin
import org.http4s.server.middleware.*
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.config.CorsConfig

// testOnly net.yoshinorin.qualtet.http.CorsProviderSpec
class CorsProviderSpec extends AnyWordSpec {

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

    "returns default origin if config includes invalid schema" in {
      val corsProvider = new CorsProvider(corsConfig =
        CorsConfig(allowOrigins =
          List(
            "http://invalid:invalid"
          )
        )
      )
      assert(
        corsProvider.origins === Set(
          Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), None)
        )
      )
    }
  }
}
