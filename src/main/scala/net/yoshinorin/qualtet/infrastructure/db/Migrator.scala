package net.yoshinorin.qualtet.infrastructure.db

import cats.Monad
import cats.effect.IO
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.contentTypes.ContentType

class Migrator() {

  /**
   * Do migration
   */
  def migrate[F[_]: Monad](contentTypeService: ContentTypeService[F]): IO[Unit] = {
    (for {
      _ <- contentTypeService.create(ContentType(name = "article"))
      _ <- contentTypeService.create(ContentType(name = "page"))
    } yield ())
  }
}
