package net.yoshinorin.qualtet.infrastructure.db

import cats.Monad
import net.yoshinorin.qualtet.config.DBConfig
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.contentTypes.ContentType
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration

class Migrator(config: DBConfig) {

  private val flywayConfig: FluentConfiguration = Flyway.configure().dataSource(config.url, config.user, config.password)
  private val flyway: Flyway = new Flyway(flywayConfig)

  /**
   * Do migration
   */
  def migrate[F[_]: Monad](contentTypeService: ContentTypeService[F]) = {
    val _ = flyway.migrate()
    (for {
      _ <- contentTypeService.create(ContentType(name = "article"))
      _ <- contentTypeService.create(ContentType(name = "page"))
    } yield ())
  }

  /**
   * DROP all tables
   * NOTE: for development
   */
  def clean(): Unit = {
    val _ = flyway.clean()
  }

  /**
   * DROP all tables and re-create
   * NOTE: for development
   */
  def recrate[F[_]: Monad](contentTypeService: ContentTypeService[F]) = {
    this.clean()
    this.migrate(contentTypeService)
  }

}
