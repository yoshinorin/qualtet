package net.yoshinorin.qualtet.syntax

import cats.data.EitherT
import cats.effect.IO

trait eitherT {

  implicit final class ValidationOps[A](v: EitherT[IO, Throwable, A]) {
    def andThrow: IO[A] = {
      v.value.flatMap {
        case Right(v: A) => IO(v)
        case Left(t: Throwable) => IO.raiseError(t)
      }
    }
  }

}
