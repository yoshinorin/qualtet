package net.yoshinorin.qualtet.validator

import cats.data.EitherT
import cats.effect.IO

object Validator {

  /**
   * @param a value
   * @param cond function for validate condition
   * @param fail Instance for Fail.
   * @return validation result with EitherT
   */
  def validate[A, F](a: A)(cond: A => Boolean)(fail: F): EitherT[IO, F, A] = {
    if (cond(a)) {
      EitherT.right(IO(a))
    } else {
      EitherT.left(IO(fail))
    }
  }

  /**
   * Opposite of validate function
   *
   * @param a value
   * @param cond function for validate condition
   * @param fail Instance for Fail.
   * @return validation result with EitherT
   */
  def validateUnless[A, F](a: A)(cond: A => Boolean)(fail: F): EitherT[IO, F, A] = {
    if (cond(a)) {
      EitherT.left(IO(fail))
    } else {
      EitherT.right(IO(a))
    }
  }

}
