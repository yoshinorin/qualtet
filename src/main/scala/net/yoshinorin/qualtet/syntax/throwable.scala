package net.yoshinorin.qualtet.syntax

import cats.Monad
import org.slf4j.LoggerFactory

trait throwable {

  private val logger = LoggerFactory.getLogger(this.getClass)

  extension (e: Throwable) {
    def withLog[F[_]: Monad]: F[Throwable] = {
      logger.error(e.getMessage)
      Monad[F].pure(e)
    }

    def logWithStackTrace[F[_]: Monad]: F[Throwable] = {
      val stackTraceString = e.getStackTrace().map(x => x.toString).mkString
      logger.error(stackTraceString)
      Monad[F].pure(e)
    }
  }

}
