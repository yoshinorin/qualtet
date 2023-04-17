package net.yoshinorin.qualtet.syntax

import com.github.plokhotnyuk.jsoniter_scala.core._
import java.nio.charset.Charset

trait json {

  extension [T](s: String)(using j: JsonValueCodec[T]) {
    def decode: T = readFromArray(s.getBytes(Charset.forName("UTF-8")))
  }

  extension [T](t: T)(using j: JsonValueCodec[T]) {
    def asJson: String = new String(writeToArray(t), Charset.forName("UTF-8"))
  }

}
