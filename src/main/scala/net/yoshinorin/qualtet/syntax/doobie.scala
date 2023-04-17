package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.actions.{Action, ActionDoobieImpl => Impl}
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext

trait doobie {

  extension [R](serviceLogic: Action[R]) {
    def perform: ConnectionIO[R] = Impl.performWithoutTransaction(serviceLogic)
  }

  extension [T](connectionIO: ConnectionIO[T]) {
    def andTransact(dbContext: DataBaseContext[Aux[IO, Unit]]): IO[T] = connectionIO.transact(dbContext.transactor)
  }

}
