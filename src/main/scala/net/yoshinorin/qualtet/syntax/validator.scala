package net.yoshinorin.qualtet.syntax

import cats.data.EitherT
import cats.effect.IO
import net.yoshinorin.qualtet.validator.Validator

trait validator {

  extension [A, F](a: A) {
    def toEitherIO(cond: A => Boolean)(left: F): EitherT[IO, F, A] = {
      Validator.validate(a)(cond)(left)
    }
  }

}
