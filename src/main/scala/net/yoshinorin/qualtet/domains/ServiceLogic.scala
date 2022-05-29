package net.yoshinorin.qualtet.domains

import cats.effect.IO
import doobie.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.domains.repository.Repository
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

sealed trait ServiceLogic[R]
final case class Continue[T, R](request: RepositoryRequest[T], next: T => ServiceLogic[R]) extends ServiceLogic[R]
final case class Done[R](value: R) extends ServiceLogic[R]

object ServiceLogic {

  // with transaction
  def runWithTransaction[R](serviceLogic: ServiceLogic[R])(doobieContext: DoobieContextBase): IO[R] = serviceLogic match {
    case continue: Continue[_, R] => runContinueWithTransaction(continue)(doobieContext)
    case Done(value) => IO(value)
  }

  private def runContinueWithTransaction[T, R](continue: Continue[T, R])(doobieContext: DoobieContextBase): IO[R] = {
    Repository.dispatch(continue.request).transact(doobieContext.transactor).flatMap { t => runWithTransaction(continue.next(t))(doobieContext) }
  }

  // without transaction
  def runWithoutTransaction[R](serviceLogic: ServiceLogic[R]): ConnectionIO[R] = serviceLogic match {
    case continue: Continue[_, R] => runContinueWithoutTransaction(continue)
  }

  private def runContinueWithoutTransaction[T, R](continue: Continue[T, R]): ConnectionIO[R] = {
    Repository.dispatch(continue.request).flatMap { t => runWithoutTransaction(continue.next(t)) }
  }
}
