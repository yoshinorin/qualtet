package net.yoshinorin.qualtet.infrastructure.db

import net.yoshinorin.qualtet.config.Config
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration

object Migration {

  private[this] val flywayConfig: FluentConfiguration = Flyway.configure().dataSource(Config.dbUrl, Config.dbUser, Config.dbPassword)
  private[this] val flyway: Flyway = new Flyway(flywayConfig)

  /**
   * Do migration
   */
  def migrate(): Unit = flyway.migrate()

  /**
   * DROP all tables
   * NOTE: for development
   */
  def clean(): Unit = flyway.clean()

  /**
   * DROP all tables and re-create
   * NOTE: for development
   */
  def recrate(): Unit = {
    this.clean()
    this.migrate()
  }

}
