package net.yoshinorin.qualtet.validator

import cats.data.EitherT
import cats.effect.IO

object Compositions {

  implicit class Optional[A](v: EitherT[IO, Throwable, A]) {

    def andThrow: IO[A] = {
      v.value.flatMap {
        case Right(v: A) => IO(v)
        case Left(t: Throwable) => IO.raiseError(t)
      }
    }

  }
}
