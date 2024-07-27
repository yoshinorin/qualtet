package net.yoshinorin.qualtet.domains.contentTaggings

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.tags.TagId
import net.yoshinorin.qualtet.infrastructure.db.Executer

class ContentTaggingService[F[_]: Monad](
  contentTaggingRepository: ContentTaggingRepository[F]
)(using executer: Executer[F, IO]) {

  def findByTagIdCont(id: TagId): ContT[F, Seq[ContentTagging], Seq[ContentTagging]] = {
    ContT.apply[F, Seq[ContentTagging], Seq[ContentTagging]] { next =>
      contentTaggingRepository.findByTagId(id)
    }
  }

  def bulkUpsertCont(data: Option[List[ContentTagging]]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      data match {
        case Some(d) => contentTaggingRepository.bulkUpsert(d)
        case None => contentTaggingRepository.fakeRequestInt
      }
    }
  }

  def deleteByContentIdCont(id: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentTaggingRepository.deleteByContentId(id)
    }
  }

  def deleteByTagIdCont(id: TagId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentTaggingRepository.deleteByTagId(id)
    }
  }

  def deleteCont(contentId: ContentId, tagIds: Seq[TagId]): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentTaggingRepository.delete(contentId, tagIds)
    }
  }

  def bulkDeleteCont(data: (ContentId, Seq[TagId])): ContT[F, Unit, Unit] = {
    data._2.size match {
      case 0 => ContT.apply[F, Unit, Unit] { next => contentTaggingRepository.fakeRequestUnit }
      case _ => this.deleteCont(data._1, data._2)
    }
  }

  def findByTagId(id: TagId): IO[Seq[ContentTagging]] = {
    executer.transact(findByTagIdCont(id))
  }
}
