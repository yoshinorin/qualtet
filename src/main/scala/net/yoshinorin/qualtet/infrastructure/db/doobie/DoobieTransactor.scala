package net.yoshinorin.qualtet.infrastructure.db.doobie

import cats.effect.{IO, Resource}
import doobie.*
import doobie.util.transactor.Transactor.Aux
import doobie.hikari.*
import com.zaxxer.hikari.HikariConfig
import net.yoshinorin.qualtet.config.DBConfig

trait DoobieTransactor[F[G[_], _]] {
  def make(config: DBConfig): Resource[IO, HikariTransactor[IO]]
}

object DoobieTransactor {

  given DoobieTransactor: DoobieTransactor[Aux] = {
    new DoobieTransactor[Aux] {
      override def make(config: DBConfig): Resource[IO, HikariTransactor[IO]] = {
        for {
          hikariConfig <- Resource.pure {
            // https://github.com/brettwooldridge/HikariCP/blob/2883f846544f88ef505d6237b0cf67131a7323c1/src/main/java/com/zaxxer/hikari/HikariConfig.java
            val hConfig = new HikariConfig()
            hConfig.setDriverClassName("org.mariadb.jdbc.Driver")
            hConfig.setJdbcUrl(config.url.toString())
            hConfig.setUsername(config.user.toString())
            hConfig.setPassword(config.password.toString())
            hConfig.setMaxLifetime(config.connectionPool.maxLifetime)
            hConfig.setMaximumPoolSize(config.connectionPool.maximumPoolSize)
            hConfig
          }
          xa <- HikariTransactor.fromHikariConfig[IO](hikariConfig)
        } yield xa
      }
    }
  }

}
