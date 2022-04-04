package net.yoshinorin.qualtet.validator

import cats.data.EitherT
import cats.effect.IO
import org.slf4j.LoggerFactory

object Validator {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * @param a value
   * @param f function for validate condition
   * @param fail Instance of Fail
   * @return validation result with EitherT
   */
  def validate[A](a: A)(f: A => Boolean)(throwable: Throwable): EitherT[IO, Throwable, A] = {
    if (f(a)) {
      EitherT.right(IO(a))
    } else {
      // TODO: Maybe should not logging here
      logger.error(throwable.getMessage)
      EitherT.left(IO(throwable))
    }
  }

  /**
   * Opposite of validate function
   *
   * @param a value
   * @param f function for validate condition
   * @param fail Instance of Fail
   * @return validation result with EitherT
   */
  def validateUnless[A](a: A)(f: A => Boolean)(throwable: Throwable): EitherT[IO, Throwable, A] = {
    if (f(a)) {
      // TODO: Maybe should not logging here
      logger.error(throwable.getMessage)
      EitherT.left(IO(throwable))
    } else {
      EitherT.right(IO(a))
    }
  }

}
