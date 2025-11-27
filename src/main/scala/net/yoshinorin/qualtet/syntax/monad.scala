package net.yoshinorin.qualtet.syntax

import cats.{Monad, MonadError}
import scala.reflect.ClassTag
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps

trait monad {

  extension [F[_]: Monad, A: ClassTag](monad: F[Option[A]]) {
    def throwIfNone[T <: Throwable](t: T): MonadError[F, Throwable] ?=> F[A] = {
      monad.flatMap {
        case Some(a: A) => Monad[F].pure(a)
        case _ => MonadError[F, Throwable].raiseError(t)
      }
    }

    def errorIfNone[E <: Exception](error: E): F[Either[E, A]] = {
      monad.map {
        case Some(a: A) => Right(a)
        case None => Left(error)
      }
    }
  }

}
