package net.yoshinorin.qualtet.actions

import cats.Monad

sealed trait Action[R]
final case class Continue[F[_]: Monad, R, T](request: F[T], next: T => Action[R]) extends Action[R]
final case class Done[R](value: R) extends Action[R]

object Action {

  def done[T]: T => Done[T] = {
    val done: T => Done[T] = (rh: T) => {
      Done(rh)
    }
    done
  }

}
