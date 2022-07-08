package net.yoshinorin.qualtet.domains

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import doobie.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

sealed trait Action[R]
final case class Continue[T, R](request: RepositoryRequest[T], next: T => Action[R]) extends Action[R]
final case class Done[R](value: R) extends Action[R]

object Action {

  // with transaction
  def transact[R](serviceLogic: Action[R])(doobieContext: DoobieContextBase): IO[R] = serviceLogic match {
    case continue: Continue[_, R] => runContinueWithTransaction(continue)(doobieContext)
    case Done(value) => IO(value)
  }

  private def runContinueWithTransaction[T, R](continue: Continue[T, R])(doobieContext: DoobieContextBase): IO[R] = {
    continue.request.dispatch.transact(doobieContext.transactor).flatMap { t => transact(continue.next(t))(doobieContext) }
  }

  // without transaction
  def connect[R](serviceLogic: Action[R]): ConnectionIO[R] = serviceLogic match {
    case continue: Continue[_, R] => runContinueWithoutTransaction(continue)
    case done: Done[R] => done.value.pure[ConnectionIO] // NOTE: Can I use pure here? I have to investigate what is `pure`.
  }

  private def runContinueWithoutTransaction[T, R](continue: Continue[T, R]): ConnectionIO[R] = {
    continue.request.dispatch.flatMap { t => connect(continue.next(t)) }
  }

  implicit class ActionOps[R](serviceLogic: Action[R]) {
    def transact()(doobieContext: DoobieContextBase): IO[R] = Action.transact(serviceLogic)(doobieContext)
    def connect(): ConnectionIO[R] = Action.connect(serviceLogic)
  }
}
