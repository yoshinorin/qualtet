package net.yoshinorin.qualtet.infrastructure.db.doobie

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.ExecutionContextExecutor
import doobie._
import cats.effect._
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.config.Config

// TODO: refactor
trait DoobieContextBase {

  val executors: ExecutorService = Executors.newCachedThreadPool()
  val executionContexts: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(executors)

  implicit val cs: ContextShift[IO] = IO.contextShift(executionContexts)

  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.mariadb.jdbc.Driver",
    Config.dbUrl,
    Config.dbUser,
    Config.dbPassword
  )

}

// TODO: refactor
class DoobieContext extends DoobieContextBase {}
