package net.yoshinorin.qualtet.syntax

import cats.Monad
import cats.syntax.all.toFunctorOps
import org.typelevel.log4cats.SelfAwareStructuredLogger

trait logger {

  enum LogLevel {
    case Debug, Info, Warn, Error
  }

  extension [E <: Exception, A](either: Either[E, A]) {
    def logLeft[F[_]: Monad](level: LogLevel, message: String)(using logger: SelfAwareStructuredLogger[F]): F[Either[E, A]] = {
      either match {
        case Right(_) =>
          Monad[F].pure(either)
        case Left(error) =>
          val logMessage = s"$message: ${error.getMessage()}"
          level match {
            case LogLevel.Debug => logger.debug(logMessage).map(_ => either)
            case LogLevel.Info => logger.info(logMessage).map(_ => either)
            case LogLevel.Warn => logger.warn(logMessage).map(_ => either)
            case LogLevel.Error => logger.error(logMessage).map(_ => either)
          }
      }
    }
  }

}
