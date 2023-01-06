package net.yoshinorin.qualtet.infrastructure.db

import cats.Monad
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.contentTypes.{ContentType, ContentTypeService}
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import cats.effect.unsafe.implicits.global

class Migrator {

  private[this] val flywayConfig: FluentConfiguration = Flyway.configure().dataSource(Config.dbUrl, Config.dbUser, Config.dbPassword)
  private[this] val flyway: Flyway = new Flyway(flywayConfig)

  /**
   * Do migration
   */
  def migrate[F[_]: Monad](contentTypeService: ContentTypeService[F]): Unit = {
    val _ = flyway.migrate()
    (for {
      _ <- contentTypeService.create(ContentType(name = "article"))
      _ <- contentTypeService.create(ContentType(name = "page"))
    } yield ()).unsafeRunSync()
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
  def recrate[F[_]: Monad](contentTypeService: ContentTypeService[F]): Unit = {
    this.clean()
    this.migrate(contentTypeService)
  }

}
