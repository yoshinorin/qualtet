package net.yoshinorin.qualtet.syntax

import cats.{Monad, MonadError}
import cats.data.EitherT
import cats.syntax.flatMap.toFlatMapOps

import scala.reflect.ClassTag

trait eitherT {

  extension [A: ClassTag, B <: Throwable, F[_]: Monad](v: EitherT[F, B, A]) {
    def andThrow: MonadError[F, Throwable] ?=> F[A] = {
      v.value.flatMap {
        case Right(v) => Monad[F].pure(v)
        case Left(t: Throwable) => MonadError[F, Throwable].raiseError(t)
      }
    }
  }

}
