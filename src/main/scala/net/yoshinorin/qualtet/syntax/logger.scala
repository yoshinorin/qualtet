package net.yoshinorin.qualtet.syntax

import cats.Monad
import cats.syntax.all.toFunctorOps
import org.typelevel.log4cats.SelfAwareStructuredLogger

enum LogLevel {
  case Debug, Info, Warn, Error
}

trait logger {

  export LogLevel.*

  extension [E <: Exception, A](either: Either[E, A]) {
    def logLeft[F[_]: Monad](level: LogLevel)(using logger: SelfAwareStructuredLogger[F]): F[Either[E, A]] = {
      either match {
        case Right(_) =>
          Monad[F].pure(either)
        case Left(error) =>
          val logMessage = s"${error.getMessage()}"
          level match {
            case LogLevel.Debug => logger.debug(error)(logMessage).map(_ => either)
            case LogLevel.Info => logger.info(error)(logMessage).map(_ => either)
            case LogLevel.Warn => logger.warn(error)(logMessage).map(_ => either)
            case LogLevel.Error => logger.error(error)(logMessage).map(_ => either)
          }
      }
    }
  }

  extension [F[_]: Monad, E <: Exception, A](fEither: F[Either[E, A]]) {
    def logLeftF(level: LogLevel)(using logger: SelfAwareStructuredLogger[F]): F[Either[E, A]] = {
      Monad[F].flatMap(fEither)(_.logLeft[F](level))
    }
  }

}
