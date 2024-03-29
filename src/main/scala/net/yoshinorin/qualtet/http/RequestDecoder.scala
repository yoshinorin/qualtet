package net.yoshinorin.qualtet.http

import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.syntax.*
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

trait RequestDecoder {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def decode[T <: Request[T]](maybeJsonString: String): JsonValueCodec[T] ?=> Either[Fail, T] = {
    try {
      Right(maybeJsonString.decode.postDecode)
    } catch {
      case NonFatal(t) =>
        logger.error(t.getMessage())
        t match {
          case t: Fail => Left(t)
          case t: JsonReaderException =>
            // TODO: consider error message
            Left(Fail.BadRequest(detail = "Wrong JSON format or missing required field. Please see API document."))
          case _ => Left(InternalServerError())
        }
    }
  }

}
