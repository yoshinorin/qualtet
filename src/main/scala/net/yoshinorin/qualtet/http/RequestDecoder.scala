package net.yoshinorin.qualtet.http

import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.syntax._
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

trait RequestDecoder {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def decode[T <: Request[T]](string: String)(implicit j: JsonValueCodec[T]): Either[Fail, T] = {
    try {
      Right(string.decode.postDecode)
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
