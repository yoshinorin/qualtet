package net.yoshinorin.qualtet.domains

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import doobie.implicits._
import doobie.ConnectionIO
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext

sealed trait DoobieAction[R]
final case class DoobieContinue[T, R](request: ConnectionIO[T], next: T => DoobieAction[R]) extends DoobieAction[R]
final case class DoobieDone[R](value: R) extends DoobieAction[R]

object DoobieAction {

  def buildDoneWithoutAnyHandle[T]: T => DoobieAction[T] = {
    val next: T => DoobieAction[T] = (rh: T) => {
      DoobieDone(rh)
    }
    next
  }

  // without transaction
  def performWithoutTransaction[R](serviceLogic: DoobieAction[R]): ConnectionIO[R] = serviceLogic match {
    case continue: DoobieContinue[_, R] => runContinueWithoutTransaction(continue)
    case done: DoobieDone[R] => done.value.pure[ConnectionIO] // NOTE: Can I use pure here? I have to investigate what is `pure`.
  }

  private def runContinueWithoutTransaction[T, R](continue: DoobieContinue[T, R]): ConnectionIO[R] = {
    continue.request.flatMap { t => performWithoutTransaction(continue.next(t)) }
  }

  implicit class ActionOps[R](serviceLogic: DoobieAction[R]) {
    // def transact()(dbContext: DataBaseContext[Aux[IO, Unit]]): IO[R] = Action.performWithTransaction(serviceLogic)(dbContext)
    def perform: ConnectionIO[R] = DoobieAction.performWithoutTransaction(serviceLogic)
  }

  implicit class ConnectionOps[T](connectionIO: ConnectionIO[T]) {
    def andTransact(dbContext: DataBaseContext[Aux[IO, Unit]]): IO[T] = connectionIO.transact(dbContext.transactor)
  }
}
