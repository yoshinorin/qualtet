package net.yoshinorin.qualtet.syntax

import net.yoshinorin.qualtet.validator.Validator

import cats.Monad
import cats.data.EitherT

trait validator {

  extension [A, B, F[_]: Monad](a: A) {
    def toEitherF(cond: A => Boolean)(left: B): EitherT[F, B, A] = {
      Validator.validate(a)(cond)(left)
    }
  }

}
