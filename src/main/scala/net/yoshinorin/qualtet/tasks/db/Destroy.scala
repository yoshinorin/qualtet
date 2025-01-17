package net.yoshinorin.qualtet.tasks.db

import cats.effect.{ExitCode, IO, IOApp}
import net.yoshinorin.qualtet.Modules

object Destroy extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    Modules.transactorResource.use { tx =>
      val modules = new Modules(tx)
      modules.migrator.clean()
      IO(ExitCode.Success)
    }
  }
}
