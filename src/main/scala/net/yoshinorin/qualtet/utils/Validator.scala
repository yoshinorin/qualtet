package net.yoshinorin.qualtet.utils

import cats.data.EitherT
import cats.effect.IO
import net.yoshinorin.qualtet.domains.models.Fail
import org.slf4j.LoggerFactory

object Validator {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * @param a value
   * @param f function for validate condition
   * @param fail Instance of Fail
   * @return validation result with EitherT
   */
  def validate[A](a: A)(f: A => Boolean)(fail: Fail): EitherT[IO, Throwable, A] = {
    if (f(a)) {
      EitherT.right(IO(a))
    } else {
      // TODO: Maybe should not logging here
      logger.error(fail.getMessage)
      EitherT.left(IO(fail))
    }
  }

}
