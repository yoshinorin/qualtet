package net.yoshinorin.qualtet.validator

import cats.data.EitherT
import cats.effect.IO

object Validator {

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
      EitherT.left(IO(throwable))
    } else {
      EitherT.right(IO(a))
    }
  }

}
