package net.yoshinorin.qualtet.infrastructure.db.doobie

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.ExecutionContextExecutor
import doobie._
import doobie.quill.DoobieContext
import io.getquill.{idiom => _, _}
import cats.effect._
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.config.Config

class DoobieContext {

  val executors: ExecutorService = Executors.newCachedThreadPool()
  val executionContexts: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(executors)

  implicit val cs: ContextShift[IO] = IO.contextShift(executionContexts)

  val ctx: DoobieContext.MySQL[SnakeCase] = new DoobieContext.MySQL(SnakeCase)

  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.mariadb.jdbc.Driver",
    Config.dbUrl,
    Config.dbUser,
    Config.dbPassword
  )
}
