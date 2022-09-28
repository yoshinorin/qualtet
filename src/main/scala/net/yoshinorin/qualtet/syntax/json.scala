package net.yoshinorin.qualtet.syntax

import com.github.plokhotnyuk.jsoniter_scala.core._
import java.nio.charset.Charset

trait json {

  implicit final class DecodeOps[T](val s: String)(implicit j: JsonValueCodec[T]) {
    def decode: T = readFromArray(s.getBytes(Charset.forName("UTF-8")))
  }

  implicit final class AsJsonOps[T](val t: T)(implicit j: JsonValueCodec[T]) {
    def asJson: String = new String(writeToArray(t), Charset.forName("UTF-8"))
  }

}
