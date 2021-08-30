package net.yoshinorin.qualtet.infrastructure.db

import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentType
import net.yoshinorin.qualtet.domains.services.ContentTypeService
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration

object Migration {

  private[this] val flywayConfig: FluentConfiguration = Flyway.configure().dataSource(Config.dbUrl, Config.dbUser, Config.dbPassword)
  private[this] val flyway: Flyway = new Flyway(flywayConfig)

  /**
   * Do migration
   */
  def migrate(contentTypeService: ContentTypeService): Unit = {
    flyway.migrate()
    contentTypeService.create(ContentType(name = "article")).unsafeRunSync()
    contentTypeService.create(ContentType(name = "page")).unsafeRunSync()
  }

  /**
   * DROP all tables
   * NOTE: for development
   */
  def clean(): Unit = flyway.clean()

  /**
   * DROP all tables and re-create
   * NOTE: for development
   */
  def recrate(contentTypeService: ContentTypeService): Unit = {
    this.clean()
    this.migrate(contentTypeService)
  }

}
