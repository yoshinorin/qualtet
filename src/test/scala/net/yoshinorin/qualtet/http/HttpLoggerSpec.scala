package net.yoshinorin.qualtet.http

import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.model.HttpRequest
// import akka.http.scaladsl.model.HttpHeader
// import akka.http.scaladsl.model.headers.Referer
// import akka.http.scaladsl.model.Uri

// testOnly net.yoshinorin.qualtet.http.HttpLoggerSpec
class HttpLoggerSpec extends AnyWordSpec with HttpLogger {

  "makeLogString" should {

    // TODO:
    /*
    "be return string for log with headers" in {
      val request = HttpRequest(headers = Seq(Referer(Uri("https://example.com"))))
      assert(makeLogString(request, "0.0.0.0", 123, "200 OK") == """"0.0.0.0" - "GET" - "/" - "200 OK" - "123ms" - "https://example.com" - """"")
    }
     */

    "be return string for log without headers" in {
      val request = HttpRequest()
      assert(makeLogString(request, "0.0.0.0", 123, "200 OK") == """"0.0.0.0" - "GET" - "/" - "200 OK" - "123ms" - "" - """"")
    }
  }

}
