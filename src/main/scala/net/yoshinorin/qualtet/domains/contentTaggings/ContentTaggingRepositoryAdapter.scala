package net.yoshinorin.qualtet.domains.contentTaggings

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

class ContentTaggingRepositoryAdapter[F[_]: Monad](
  contentTaggingRepository: ContentTaggingRepository[F]
) {

  private[domains] def findByTagId(id: TagId): ContT[F, Seq[ContentTagging], Seq[ContentTagging]] = {
    ContT.apply[F, Seq[ContentTagging], Seq[ContentTagging]] { _ =>
      contentTaggingRepository.findByTagId(id).map { ct =>
        ct.map(c => ContentTagging(c.contentId, c.tagId))
      }
    }
  }

  private[domains] def bulkUpsert(data: List[ContentTagging]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { _ =>
      data.size match {
        case 0 => Monad[F].pure(0)
        case _ => {
          val ws = data.map(c => ContentTaggingWriteModel(c.contentId, c.tagId))
          contentTaggingRepository.bulkUpsert(ws)
        }
      }
    }
  }

  private[domains] def deleteByContentId(id: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      contentTaggingRepository.deleteByContentId(id)
    }
  }

  private[domains] def deleteByTagId(id: TagId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      contentTaggingRepository.deleteByTagId(id)
    }
  }

  private[domains] def delete(contentId: ContentId, tagIds: Seq[TagId]): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      contentTaggingRepository.delete(contentId, tagIds)
    }
  }

  private[domains] def bulkDelete(data: (ContentId, Seq[TagId])): ContT[F, Unit, Unit] = {
    data._2.size match {
      case 0 => ContT.apply[F, Unit, Unit] { _ => Monad[F].pure(()) }
      case _ => this.delete(data._1, data._2)
    }
  }
}
