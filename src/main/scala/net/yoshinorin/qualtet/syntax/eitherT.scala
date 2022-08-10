package net.yoshinorin.qualtet.syntax

import cats.data.EitherT
import cats.effect.IO
import scala.reflect.ClassTag

trait eitherT {

  implicit final class EitherTOps[A: ClassTag, F <: Throwable](v: EitherT[IO, F, A]) {
    def andThrow: IO[A] = {
      v.value.flatMap {
        case Right(v) => IO(v)
        case Left(t: Throwable) => IO.raiseError(t)
      }
    }
  }

}
