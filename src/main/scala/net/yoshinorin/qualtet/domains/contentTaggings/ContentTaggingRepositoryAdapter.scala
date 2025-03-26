package net.yoshinorin.qualtet.domains.contentTaggings

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId

class ContentTaggingRepositoryAdapter[F[_]: Monad](
  contentTaggingRepository: ContentTaggingRepository[F]
) {

  def findByTagId(id: TagId): ContT[F, Seq[ContentTagging], Seq[ContentTagging]] = {
    ContT.apply[F, Seq[ContentTagging], Seq[ContentTagging]] { next =>
      contentTaggingRepository.findByTagId(id).map { ct =>
        ct.map(c => ContentTagging(c.contentId, c.tagId))
      }
    }
  }

  def bulkUpsert(data: Option[List[ContentTagging]]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      data match {
        case Some(d) => {
          val ws = d.map(c => ContentTaggingWriteModel(c.contentId, c.tagId))
          contentTaggingRepository.bulkUpsert(ws)
        }
        case None => Monad[F].pure(0)
      }
    }
  }

  def deleteByContentId(id: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentTaggingRepository.deleteByContentId(id)
    }
  }

  def deleteByTagId(id: TagId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentTaggingRepository.deleteByTagId(id)
    }
  }

  def delete(contentId: ContentId, tagIds: Seq[TagId]): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentTaggingRepository.delete(contentId, tagIds)
    }
  }

  def bulkDelete(data: (ContentId, Seq[TagId])): ContT[F, Unit, Unit] = {
    data._2.size match {
      case 0 => ContT.apply[F, Unit, Unit] { _ => Monad[F].pure(()) }
      case _ => this.delete(data._1, data._2)
    }
  }
}
