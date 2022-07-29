package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import scala.reflect.ClassTag

trait io {

  implicit final class IOOps[A : ClassTag](val io: IO[Option[A]]) {
    def throwIfNone[F <: Throwable](t: F): IO[A] = {
      io.flatMap {
        case Some(a: A) => IO(a)
        case _ => IO.raiseError(t)
      }
    }
  }

}
