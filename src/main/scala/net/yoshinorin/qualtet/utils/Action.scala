package net.yoshinorin.qualtet.utils

sealed trait Action[R]
final case class Continue[T, R, F[_]](request: F[T], next: T => Action[R]) extends Action[R]
final case class Done[R](value: R) extends Action[R]

object Action {

  def buildDoneWithoutAnyHandle[T]: T => Action[T] = {
    val next: T => Action[T] = (rh: T) => {
      Done(rh)
    }
    next
  }

}
