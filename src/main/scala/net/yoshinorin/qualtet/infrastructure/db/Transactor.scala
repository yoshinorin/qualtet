package net.yoshinorin.qualtet.infrastructure.db

import cats.Monad
import net.yoshinorin.qualtet.actions.Action

trait Transactor[F[_], G[_]: Monad] {
  def perform[R](a: Action[R]): F[R]
  def transact[T](t: F[T]): G[T]
  def transact[R](a: Action[R]): G[R]
  def transact2[T1, T2](ts: (F[(T1, T2)])): G[T2]
  def transact4[T1, T2, T3, T4](ts: (F[(T1, T2, T3, T4)])): G[T4]
  def transact8[T1, T2, T3, T4, T5, T6, T7, T8](ts: (F[(T1, T2, T3, T4, T5, T6, T7, T8)])): G[T8]
}
