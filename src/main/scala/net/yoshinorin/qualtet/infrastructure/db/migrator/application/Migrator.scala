package net.yoshinorin.qualtet.infrastructure.db.migrator.application

import cats.Monad
import cats.effect.IO
import net.yoshinorin.qualtet.domains.contentTypes.{ContentType, ContentTypeName, ContentTypeService}

class Migrator() {

  /**
   * Do migration
   */
  def migrate[F[_]: Monad](contentTypeService: ContentTypeService[F]): IO[Unit] = {
    (for {
      _ <- contentTypeService.create(ContentType(name = ContentTypeName("article")))
      _ <- contentTypeService.create(ContentType(name = ContentTypeName("page")))
    } yield ())
  }
}
