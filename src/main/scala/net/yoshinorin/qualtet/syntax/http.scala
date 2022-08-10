package net.yoshinorin.qualtet.syntax

import akka.http.scaladsl.model.HttpHeader
import java.util.Locale

trait http {

  implicit final class HttpHeadersOps(headers: Seq[HttpHeader]) {
    def extract(name: String): Option[HttpHeader] = {
      headers.find(h => h.is(name.toLowerCase(Locale.ENGLISH)))
    }

    def userAgent: Option[HttpHeader] = this.extract("User-Agent")
    def referer: Option[HttpHeader] = this.extract("Referer")
  }

  implicit final class HttpHeaderOptionOps(maybeHeader: Option[HttpHeader]) {
    @SuppressWarnings(Array("org.wartremover.warts.ToString"))
    def stringify(): String = maybeHeader.getOrElse("").toString()
  }

}
