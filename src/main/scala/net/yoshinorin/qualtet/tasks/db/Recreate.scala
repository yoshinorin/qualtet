package net.yoshinorin.qualtet.tasks.db

import cats.effect.{ExitCode, IO, IOApp}
import net.yoshinorin.qualtet.Modules

import cats.effect.unsafe.implicits.global

object Recreate extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    Modules.transactorResource(None).use { tx =>
      val modules = new Modules(tx)
      (for {
        _ <- IO(modules.flywayMigrator.clean())
        _ <- IO(modules.flywayMigrator.migrate())
        _ <- IO(modules.migrator.migrate(modules.contentTypeService))
      } yield ()).unsafeRunSync()
      IO(ExitCode.Success)
    }
  }
}
