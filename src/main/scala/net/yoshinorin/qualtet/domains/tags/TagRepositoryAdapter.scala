package net.yoshinorin.qualtet.domains.tags

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentId

class TagRepositoryAdapter[F[_]: Monad](
  tagRepository: TagRepository[F]
) {

  def bulkUpsert(data: Option[List[Tag]]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      data match {
        case Some(d) => {
          val ws = d.map { t => TagWriteModel(id = t.id, name = t.name) }
          tagRepository.bulkUpsert(ws)
        }
        case None => Monad[F].pure(0)
      }
    }
  }

  def getAll: ContT[F, Seq[TagResponseModel], Seq[TagResponseModel]] = {
    ContT.apply[F, Seq[TagResponseModel], Seq[TagResponseModel]] { next =>
      tagRepository.getAll().map { x =>
        x.map { case (cnt, tag) => TagResponseModel(count = cnt, id = tag.id, name = tag.name) }
      }
    }
  }

  def findById(id: TagId): ContT[F, Option[Tag], Option[Tag]] = {
    ContT.apply[F, Option[Tag], Option[Tag]] { next =>
      tagRepository.findById(id).map { x =>
        x.map { t =>
          Tag(t.id, t.name)
        }
      }
    }
  }

  def findByName(tagName: TagName): ContT[F, Option[Tag], Option[Tag]] = {
    ContT.apply[F, Option[Tag], Option[Tag]] { next =>
      tagRepository.findByName(tagName).map { x =>
        x.map { t =>
          Tag(t.id, t.name)
        }
      }
    }
  }

  def findByContentId(contenId: ContentId): ContT[F, Seq[Tag], Seq[Tag]] = {
    ContT.apply[F, Seq[Tag], Seq[Tag]] { next =>
      tagRepository.findByContentId(contenId).map { x =>
        x.map { t =>
          Tag(t.id, t.name)
        }
      }
    }
  }

  def delete(id: TagId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      tagRepository.delete(id)
    }
  }

}
