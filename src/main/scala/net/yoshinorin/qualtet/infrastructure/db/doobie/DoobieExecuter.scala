package net.yoshinorin.qualtet.infrastructure.db.doobie

import cats.data.ContT
import cats.implicits.catsSyntaxApplicativeId
import doobie.*
import doobie.syntax.all.*
import cats.effect.*
import net.yoshinorin.qualtet.infrastructure.db.Executer
import doobie.free.connection.ConnectionIO

class DoobieExecuter(tx: Transactor[IO]) extends Executer[ConnectionIO, IO] {

  override def perform[R](c: ContT[doobie.ConnectionIO, R, R]): ConnectionIO[R] = {
    c.run { x => x.pure[ConnectionIO] }
  }

  override def transact[R](t: ContT[doobie.ConnectionIO, R, R]): IO[R] = transact(t.run { x => x.pure[ConnectionIO] })

  override def transact[T](connectionIO: ConnectionIO[T]): IO[T] = connectionIO.transact(tx)

  override def transact2[T1, T2](ts: (ConnectionIO[(T1, T2)])): IO[T2] = {
    for {
      r <- ts.transact(tx)
    } yield r._2
  }

  override def transact5[T1, T2, T3, T4, T5](ts: (ConnectionIO[(T1, T2, T3, T4, T5)])): IO[T5] = {
    for {
      r <- ts.transact(tx)
    } yield r._5
  }

  override def transact11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](
    ts: (ConnectionIO[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)])
  ): IO[T11] = {
    for {
      r <- ts.transact(tx)
    } yield r._11
  }

}
