package net.yoshinorin.qualtet.syntax

import akka.http.scaladsl.model.HttpHeader

trait http {

  implicit final class HttpHeadersOps(headers: Seq[HttpHeader]) {
    def extract(name: String): Option[HttpHeader] = {
      headers.find(h => h.is(name.toLowerCase()))
    }

    def userAgent: Option[HttpHeader] = this.extract("User-Agent")
    def referer: Option[HttpHeader] = this.extract("Referer")
  }

}
