package net.yoshinorin.qualtet.infrastructure.db

import cats.Monad
import cats.data.ContT

trait Executer[F[_], G[_]: Monad] {
  def perform[R](a: ContT[F, R, R]): F[R]
  def transact[R](t: ContT[F, R, R]): G[R]
  def transact[T](t: F[T]): G[T]
  def transact2[T1, T2](ts: (F[(T1, T2)])): G[T2]
  def transact4[T1, T2, T3, T4](ts: (F[(T1, T2, T3, T4)])): G[T4]
  def transact11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](ts: (F[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)])): G[T11]
}
