package net.yoshinorin.qualtet.syntax

import cats.effect.IO

trait io {

  implicit final class IOOps[A](val io: IO[Option[A]]) {
    def throwIfNone[F <: Throwable](t: F): IO[A] = {
      io.flatMap {
        case None => IO.raiseError(t)
        case Some(a: A) => IO(a)
      }
    }
  }

}
