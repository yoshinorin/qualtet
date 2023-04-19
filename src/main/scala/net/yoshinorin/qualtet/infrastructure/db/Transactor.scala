package net.yoshinorin.qualtet.infrastructure.db

import cats.effect.IO
import net.yoshinorin.qualtet.actions.Action

trait Transactor[M[_]] {
  def perform[R](a: Action[R]): M[R]
  def transact[T](t: M[T]): IO[T]
  def transact[R](a: Action[R]): IO[R]
  def transact2[T1, R](ts: (M[(T1, R)])): IO[R]
  def transact4[T1, T2, T3, R](ts: (M[(T1, T2, T3, R)])): IO[R]
  def transact7[T1, T2, T3, T4, T5, T6, R](ts: (M[(T1, T2, T3, T4, T5, T6, R)])): IO[R]
}
