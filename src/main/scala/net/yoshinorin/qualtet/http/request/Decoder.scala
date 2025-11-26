package net.yoshinorin.qualtet.http.request

import cats.Monad
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.errors.{DomainError, UnexpectedException, UnexpectedJsonFormat}
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.util.control.NonFatal

trait Decoder[F[_]: Monad](using loggerFactory: Log4CatsLoggerFactory[F]) {

  private val logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def decode[T <: Request[T]](maybeJsonString: String): JsonValueCodec[T] ?=> F[Either[DomainError, T]] = {
    try {
      val decoded = maybeJsonString.decode
      Monad[F].pure { decoded.postDecode }
    } catch {
      case NonFatal(t) =>
        logger.error(t.getMessage()) *>
          Monad[F].pure {
            t match {
              case t: DomainError => Left(t)
              case t: JsonReaderException => Left(UnexpectedJsonFormat(detail = "Invalid JSON format or missing required field. Please see API document."))
              case _ => Left(UnexpectedException())
            }
          }
    }
  }

}
