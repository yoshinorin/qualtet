package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import org.slf4j.LoggerFactory

trait throwable {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  extension (e: Throwable) {
    def withLog: IO[Throwable] = {
      logger.error(e.getMessage)
      IO(e)
    }

    def logWithStackTrace: IO[Throwable] = {
      val stackTraceString = e.getStackTrace().map(x => x.toString).mkString
      logger.error(stackTraceString)
      IO(e)
    }
  }

}
