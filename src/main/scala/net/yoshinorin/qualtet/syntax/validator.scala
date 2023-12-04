package net.yoshinorin.qualtet.syntax

import cats.Monad
import cats.data.EitherT
import net.yoshinorin.qualtet.validator.Validator

trait validator {

  extension [A, B, F[_]: Monad](a: A) {
    def toEitherF(cond: A => Boolean)(left: B): EitherT[F, B, A] = {
      Validator.validate(a)(cond)(left)
    }
  }

}
