package net.yoshinorin.qualtet.infrastructure.db.doobie

import cats.effect.IO
import doobie.*
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.config.DBConfig

trait DoobieTransactor[F[G[_], _]] {
  def make(config: DBConfig): Transactor[IO]
}

object DoobieTransactor {

  given DoobieTransactor: DoobieTransactor[Aux] = {
    new DoobieTransactor[Aux] {
      override def make(config: DBConfig): Transactor[IO] = {
        Transactor.fromDriverManager[IO](
          driver = "org.mariadb.jdbc.Driver",
          url = config.url,
          user = config.user,
          password = config.password,
          logHandler = None
        )
      }
    }
  }

}
