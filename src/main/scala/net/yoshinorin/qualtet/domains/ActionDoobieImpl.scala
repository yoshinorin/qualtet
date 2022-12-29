package net.yoshinorin.qualtet.domains

import cats.implicits.catsSyntaxApplicativeId
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.{Action, Continue, Done}

object ActionDoobieImpl {

  // without transaction
  def performWithoutTransaction[R](serviceLogic: Action[R]): ConnectionIO[R] = serviceLogic match {
    case continue: Continue[_, R, ConnectionIO] => runContinueWithoutTransaction(continue)
    case done: Done[R] => done.value.pure[ConnectionIO] // NOTE: Can I use pure here? I have to investigate what is `pure`.
  }

  private def runContinueWithoutTransaction[T, R](continue: Continue[T, R, ConnectionIO]): ConnectionIO[R] = {
    continue.request.flatMap { t => performWithoutTransaction(continue.next(t)) }
  }

}
