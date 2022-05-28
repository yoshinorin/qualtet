package net.yoshinorin.qualtet.domains

import cats.effect.IO
import doobie.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.domains.repository.Repository
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

sealed trait ServiceLogic[R]
case class Continue[T, R](request: RepositoryRequest[T], next: T => ServiceLogic[R]) extends ServiceLogic[R]
case class Done[R](value: R) extends ServiceLogic[R]

object ServiceLogic {

  def run[R](serviceLogic: ServiceLogic[R])(implicit doobieContext: DoobieContextBase): IO[R] = serviceLogic match {
    case continue: Continue[_, R] => runContinue(continue)
    case Done(value) => IO(value)
  }

  private def runContinue[T, R](continue: Continue[T, R])(implicit doobieContext: DoobieContextBase): IO[R] = {
    Repository.dispatch(continue.request).transact(doobieContext.transactor).flatMap { t => run(continue.next(t)) }
  }
}
