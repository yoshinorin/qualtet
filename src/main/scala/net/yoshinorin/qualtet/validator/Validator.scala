package net.yoshinorin.qualtet.validator

import cats.Monad
import cats.data.EitherT

object Validator {

  /**
   * @param a value
   * @param cond function for validate condition
   * @param fail Instance for Fail.
   * @return validation result with EitherT
   */
  def validate[A, B, F[_]: Monad](a: A)(cond: A => Boolean)(fail: B): EitherT[F, B, A] = {
    if (cond(a)) {
      EitherT.right(Monad[F].pure(a))
    } else {
      EitherT.left(Monad[F].pure(fail))
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
  def validateUnless[A, B, F[_]: Monad](a: A)(cond: A => Boolean)(fail: B): EitherT[F, B, A] = {
    if (cond(a)) {
      EitherT.left(Monad[F].pure(fail))
    } else {
      EitherT.right(Monad[F].pure(a))
    }
  }

}
