package net.yoshinorin.qualtet.domains

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import doobie.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

sealed trait ServiceLogic[R]
final case class Continue[T, R](request: RepositoryRequest[T], next: T => ServiceLogic[R]) extends ServiceLogic[R]
final case class Done[R](value: R) extends ServiceLogic[R]

object ServiceLogic {

  // with transaction
  def transact[R](serviceLogic: ServiceLogic[R])(doobieContext: DoobieContextBase): IO[R] = serviceLogic match {
    case continue: Continue[_, R] => runContinueWithTransaction(continue)(doobieContext)
    case Done(value) => IO(value)
  }

  private def runContinueWithTransaction[T, R](continue: Continue[T, R])(doobieContext: DoobieContextBase): IO[R] = {
    continue.request.dispatch.transact(doobieContext.transactor).flatMap { t => transact(continue.next(t))(doobieContext) }
  }

  // without transaction
  def connect[R](serviceLogic: ServiceLogic[R]): ConnectionIO[R] = serviceLogic match {
    case continue: Continue[_, R] => runContinueWithoutTransaction(continue)
    case done: Done[R] => done.value.pure[ConnectionIO] // NOTE: Can I use pure here? I have to investigate what is `pure`.
  }

  private def runContinueWithoutTransaction[T, R](continue: Continue[T, R]): ConnectionIO[R] = {
    continue.request.dispatch.flatMap { t => connect(continue.next(t)) }
  }

  implicit class ServiceLogicOps[R](serviceLogic: ServiceLogic[R]) {
    def transact()(doobieContext: DoobieContextBase): IO[R] = ServiceLogic.transact(serviceLogic)(doobieContext)
    def connect(): ConnectionIO[R] = ServiceLogic.connect(serviceLogic)
  }
}
