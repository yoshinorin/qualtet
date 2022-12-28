package net.yoshinorin.qualtet.infrastructure.db.doobie

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.ExecutionContextExecutor
import doobie._
import doobie.util.transactor.Transactor.Aux
import cats.effect._
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext

class DoobieContext extends DataBaseContext[Aux[IO, Unit]] {

  val executors: ExecutorService = Executors.newCachedThreadPool()
  val executionContexts: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(executors)

  // NOTE: No-need ContextShift: https://typelevel.org/cats-effect/docs/migration-guide#contextshift
  // implicit val cs: ContextShift[IO] = IO.contextShift(executionContexts)

  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.mariadb.jdbc.Driver",
    Config.dbUrl,
    Config.dbUser,
    Config.dbPassword
  )

}
