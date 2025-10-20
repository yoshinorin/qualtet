package net.yoshinorin.qualtet.infrastructure.db.doobie

import cats.effect.{IO, Resource}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.log.LogHandler
import doobie.hikari.*
import org.typelevel.otel4s.trace.Tracer
import com.zaxxer.hikari.HikariConfig
import net.yoshinorin.qualtet.config.DBConfig
import net.yoshinorin.qualtet.infrastructure.telemetry.DoobieTracing

trait DoobieTransactor[F[G[_], _]] {
  def make(config: DBConfig, tracer: Option[Tracer[IO]]): Resource[IO, Transactor[IO]]
}

object DoobieTransactor {

  given DoobieTransactor: DoobieTransactor[Aux] = {
    new DoobieTransactor[Aux] {

      private def createHikariConfig(config: DBConfig): HikariConfig = {
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

      override def make(config: DBConfig, tracer: Option[Tracer[IO]]): Resource[IO, Transactor[IO]] = {
        val logHandler: Option[LogHandler[IO]] = tracer.map(DoobieTracing.logHandler)

        for {
          hikariConfig <- Resource.pure(createHikariConfig(config))
          xa <- HikariTransactor.fromHikariConfig[IO](hikariConfig, logHandler)
        } yield xa
      }
    }
  }

}
