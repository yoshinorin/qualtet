package net.yoshinorin.qualtet.http

import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.message.Fail
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal
import java.nio.charset.Charset

trait RequestDecoder {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def decode[T <: Request[T]](string: String)(implicit e: JsonValueCodec[T]): Either[Fail, T] = {
    try {
      // TODO: consider how to handle Charset
      val x = readFromArray(string.getBytes(Charset.forName("UTF-8")))
      x.postDecode
      Right(x)
    } catch {
      case NonFatal(t) =>
        logger.error(t.getMessage())
        t match {
          case t: Fail => Left(t)
          case t: JsonReaderException =>
            // TODO: consider error message
            Left(Fail.BadRequest(t.getMessage()))
          case _ => Left(InternalServerError())
        }
    }
  }

}
