package net.yoshinorin.qualtet.infrastructure.db

import cats.Monad
import cats.data.ContT

import scala.annotation.nowarn

/**
 * Executer bridges database transaction effects (G) to application effects (F).
 *
 * @tparam G Database transaction effect (e.g., ConnectionIO)
 * @tparam F Application effect (e.g., IO)
 */
trait Executer[G[_], F[_]: Monad @nowarn] {
  def defer[R](a: ContT[G, R, R]): G[R]
  def transact[R](t: ContT[G, R, R]): F[R]
  def transact[T](t: G[T]): F[T]
  def transact2[T1, T2](ts: (G[(T1, T2)])): F[T2]
  def transact5[T1, T2, T3, T4, T5](ts: (G[(T1, T2, T3, T4, T5)])): F[T5]
  def transact11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](ts: (G[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)])): F[T11]
}
