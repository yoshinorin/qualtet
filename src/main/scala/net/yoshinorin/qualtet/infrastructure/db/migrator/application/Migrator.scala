package net.yoshinorin.qualtet.infrastructure.db.migrator.application

import cats.Monad
import cats.effect.IO
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contentTypes.{ContentType, ContentTypeName, ContentTypeService}

class Migrator() {

  /**
   * Do migration
   */
  def migrate[F[_]: Monad](contentTypeService: ContentTypeService[F]): IO[Unit] = {
    (for {
      // FIXME: avoid using `toOption.get`
      articleResult <- contentTypeService.create(ContentType(name = ContentTypeName("article").toOption.get))
      _ <- articleResult.liftTo[IO]
      pageResult <- contentTypeService.create(ContentType(name = ContentTypeName("page").toOption.get))
      _ <- pageResult.liftTo[IO]
    } yield ())
  }
}
