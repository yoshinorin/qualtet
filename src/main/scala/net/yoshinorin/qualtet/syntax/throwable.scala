package net.yoshinorin.qualtet.syntax

import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.SelfAwareStructuredLogger

trait throwable {

  extension (e: Throwable) {
    def logWithStackTrace[F[_]: Monad](using logger: SelfAwareStructuredLogger[F]): F[Throwable] = {
      for {
        stackTraceString <- Monad[F].pure(e.getStackTrace().map(x => x.toString).mkString)
        _ <- logger.error(stackTraceString)
      } yield {
        e
      }
    }
  }

}
