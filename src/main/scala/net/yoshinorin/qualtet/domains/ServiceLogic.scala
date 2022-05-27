package net.yoshinorin.qualtet.domains

import cats.effect.IO
import doobie.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.domains.repository.Repository
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

sealed trait ServiceLogic[H]
case class Continue[T, H](request: RepositoryRequest[T], resultHandler: T => ServiceLogic[H]) extends ServiceLogic[H]
case class Done[H](value: H) extends ServiceLogic[H]

object ServiceLogic {

  def runServiceLogic[H](serviceLogic: ServiceLogic[H])(implicit doobieContext: DoobieContextBase): IO[H] = serviceLogic match {
    case continue: Continue[_, H] => runHandler(continue)
    case Done(value) => IO(value)
  }

  private def runHandler[T, H](continue: Continue[T, H])(implicit doobieContext: DoobieContextBase): IO[H] = {
    Repository.dispatch(continue.request).transact(doobieContext.transactor).flatMap {
      t => runServiceLogic(continue.resultHandler(t))
    }
  }
}

