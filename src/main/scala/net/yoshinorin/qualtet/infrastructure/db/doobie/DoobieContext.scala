package net.yoshinorin.qualtet.infrastructure.db.doobie

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.ExecutionContextExecutor
import cats.implicits.catsSyntaxApplicativeId
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import cats.effect._
import net.yoshinorin.qualtet.actions.{Action, Continue, Done}
import net.yoshinorin.qualtet.config.DBConfig
import net.yoshinorin.qualtet.infrastructure.db.Transactor

class DoobieTransactor(config: DBConfig) extends Transactor[ConnectionIO] {

  val executors: ExecutorService = Executors.newCachedThreadPool()
  val executionContexts: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(executors)

  // NOTE: No-need ContextShift: https://typelevel.org/cats-effect/docs/migration-guide#contextshift
  // implicit val cs: ContextShift[IO] = IO.contextShift(executionContexts)

  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.mariadb.jdbc.Driver",
    config.url,
    config.user,
    config.password
  )

  override def perform[R](action: Action[R]): ConnectionIO[R] = action match {
    case continue: Continue[_, R, ConnectionIO] => continue.request.flatMap { t => perform(continue.next(t)) }
    case done: Done[R] => done.value.pure[ConnectionIO]
  }

  override def transact[T](connectionIO: ConnectionIO[T]): IO[T] = connectionIO.transact(transactor)

  override def transact[R](action: Action[R]): IO[R] = transact(perform(action))

  override def transact2[T1, T2](ts: (ConnectionIO[(T1, T2)])): IO[T2] = {
    for {
      r <- ts.transact(transactor)
    } yield r._2
  }

  override def transact4[T1, T2, T3, T4](ts: (ConnectionIO[(T1, T2, T3, T4)])): IO[T4] = {
    for {
      r <- ts.transact(transactor)
    } yield r._4
  }

  override def transact7[T1, T2, T3, T4, T5, T6, T7](ts: (ConnectionIO[(T1, T2, T3, T4, T5, T6, T7)])): IO[T7] = {
    for {
      r <- ts.transact(transactor)
    } yield r._7
  }

  /*
  implicit final class PerformOps[R](action: Action[R]) {
    def perform: ConnectionIO[R] = action match {
      case continue: Continue[_, R, ConnectionIO] => continue.request.flatMap { t => continue.next(t).perform }
      case done: Done[R] => done.value.pure[ConnectionIO]
    }
  }

  implicit final class TransactOps[T](connectionIO: ConnectionIO[T]) {
    def andTransact: IO[T] = connectionIO.transact(transactor)
  }
   */
}
