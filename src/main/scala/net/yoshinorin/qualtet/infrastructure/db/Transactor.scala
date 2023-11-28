package net.yoshinorin.qualtet.infrastructure.db

import cats.effect.IO
import net.yoshinorin.qualtet.actions.Action

trait Transactor[F[_]] {
  def perform[R](a: Action[R]): F[R]
  def transact[T](t: F[T]): IO[T]
  def transact[R](a: Action[R]): IO[R]
  def transact2[T1, T2](ts: (F[(T1, T2)])): IO[T2]
  def transact4[T1, T2, T3, T4](ts: (F[(T1, T2, T3, T4)])): IO[T4]
  def transact8[T1, T2, T3, T4, T5, T6, T7, T8](ts: (F[(T1, T2, T3, T4, T5, T6, T7, T8)])): IO[T8]
}
