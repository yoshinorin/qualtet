package net.yoshinorin.qualtet.http

import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.message.Fail
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal
import java.nio.charset.Charset

trait RequestDecoder {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def decode[T](string: String)(implicit e: JsonValueCodec[T]): Either[Fail, T] = {
    try {
      // TODO: consider how to handle Charset
      Right(readFromArray(string.getBytes(Charset.forName("UTF-8"))))
    } catch {
      case NonFatal(t) =>
        logger.error(t.getMessage())
        t match {
          // TODO: may be I need re-architect of error handling (e.g. HTTP STATUS CODE 400, 422) for jsoniter
          case t: Fail => Left(t)
          case _ => Left(InternalServerError())
        }
    }
  }

}
