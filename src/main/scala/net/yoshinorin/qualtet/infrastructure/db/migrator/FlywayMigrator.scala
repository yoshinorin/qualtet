package net.yoshinorin.qualtet.infrastructure.db.migrator

import org.flywaydb.core.Flyway
import net.yoshinorin.qualtet.config.DBConfig
import org.flywaydb.core.api.configuration.FluentConfiguration

class FlywayMigrator(config: DBConfig) {

  private val flywayConfig: FluentConfiguration = Flyway.configure().dataSource(config.url, config.user, config.password).cleanDisabled(false)
  private val flyway: Flyway = new Flyway(flywayConfig)

  /**
   * Do migration
   */
  def migrate() = {
    val _ = flyway.migrate()
  }

  /**
   * DROP all tables
   * NOTE: for development
   */
  def clean(): Unit = {
    val _ = flyway.clean()
  }
}
