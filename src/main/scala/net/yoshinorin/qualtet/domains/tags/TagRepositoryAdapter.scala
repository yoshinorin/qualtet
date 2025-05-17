package net.yoshinorin.qualtet.domains.tags

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentId

class TagRepositoryAdapter[F[_]: Monad](
  tagRepository: TagRepository[F]
) {

  private[domains] def bulkUpsert(data: Option[List[Tag]]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { _ =>
      data match {
        case Some(d) => {
          val ws = d.map { t => TagWriteModel(id = t.id, name = t.name, path = t.path) }
          tagRepository.bulkUpsert(ws)
        }
        case None => Monad[F].pure(0)
      }
    }
  }

  private[domains] def getAll: ContT[F, Seq[TagResponseModel], Seq[TagResponseModel]] = {
    ContT.apply[F, Seq[TagResponseModel], Seq[TagResponseModel]] { _ =>
      tagRepository.getAll().map { x =>
        x.map { case (cnt, tag) => TagResponseModel(count = cnt, id = tag.id, name = tag.name, path = tag.path) }
      }
    }
  }

  private[domains] def findById(id: TagId): ContT[F, Option[Tag], Option[Tag]] = {
    ContT.apply[F, Option[Tag], Option[Tag]] { _ =>
      tagRepository.findById(id).map { x =>
        x.map { t =>
          Tag(t.id, t.name, t.path)
        }
      }
    }
  }

  private[domains] def findByName(tagName: TagName): ContT[F, Option[Tag], Option[Tag]] = {
    ContT.apply[F, Option[Tag], Option[Tag]] { _ =>
      tagRepository.findByName(tagName).map { x =>
        x.map { t =>
          Tag(t.id, t.name, t.path)
        }
      }
    }
  }

  private[domains] def findByContentId(contenId: ContentId): ContT[F, Seq[Tag], Seq[Tag]] = {
    ContT.apply[F, Seq[Tag], Seq[Tag]] { _ =>
      tagRepository.findByContentId(contenId).map { x =>
        x.map { t =>
          Tag(t.id, t.name, t.path)
        }
      }
    }
  }

  private[domains] def delete(id: TagId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      tagRepository.delete(id)
    }
  }

}
