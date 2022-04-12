package net.yoshinorin.qualtet

import cats.data.EitherT
import cats.effect.IO

package object syntax {

  implicit final class ValidationCompositions[A](v: EitherT[IO, Throwable, A]) {

    def andThrow: IO[A] = {
      v.value.flatMap {
        case Right(v: A) => IO(v)
        case Left(t: Throwable) => IO.raiseError(t)
      }
    }

  }
}
