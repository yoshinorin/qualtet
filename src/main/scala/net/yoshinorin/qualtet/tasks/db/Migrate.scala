package net.yoshinorin.qualtet.tasks.db

import cats.effect.{ExitCode, IO, IOApp}
import net.yoshinorin.qualtet.Modules

import cats.effect.unsafe.implicits.global

object Migrate extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    Modules.transactorResource.use { tx =>
      val modules = new Modules(tx)
      modules.migrator.migrate(modules.contentTypeService).unsafeRunSync() // FIXME
      IO(ExitCode.Success)
    }
  }
}
