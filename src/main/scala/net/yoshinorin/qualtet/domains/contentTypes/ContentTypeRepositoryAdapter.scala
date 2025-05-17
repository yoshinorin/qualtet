package net.yoshinorin.qualtet.domains.contentTypes

import cats.data.ContT
import cats.Monad
import cats.implicits.*

class ContentTypeRepositoryAdapter[F[_]: Monad](
  contentRepository: ContentTypeRepository[F]
) {

  private[domains] def findByName(name: ContentTypeName): ContT[F, Option[ContentType], Option[ContentType]] = {
    ContT.apply[F, Option[ContentType], Option[ContentType]] { _ =>
      contentRepository.findByName(name).map {
        case Some(c) => Some(ContentType(c.id, c.name))
        case None => None
      }
    }
  }

  private[domains] def upsert(data: ContentType): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { _ =>
      val w = ContentTypeWriteModel(id = data.id, name = data.name)
      contentRepository.upsert(w)
    }
  }

  private[domains] def getAll: ContT[F, Seq[ContentType], Seq[ContentType]] = {
    ContT.apply[F, Seq[ContentType], Seq[ContentType]] { _ =>
      contentRepository.getAll().map { cr =>
        cr.map { c => ContentType(c.id, c.name) }
      }
    }
  }
}
