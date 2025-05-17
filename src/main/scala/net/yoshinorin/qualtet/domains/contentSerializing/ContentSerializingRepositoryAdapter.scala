package net.yoshinorin.qualtet.domains.contentSerializing

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId

class ContentSerializingRepositoryAdapter[F[_]: Monad](
  contentSerializingRepository: ContentSerializingRepository[F]
) {

  private[domains] def findBySeriesId(id: SeriesId): ContT[F, Seq[ContentSerializing], Seq[ContentSerializing]] = {
    ContT.apply[F, Seq[ContentSerializing], Seq[ContentSerializing]] { _ =>
      contentSerializingRepository.findBySeriesId(id).map { cs =>
        cs.map(c => ContentSerializing(c.seriesId, c.contentId))
      }
    }
  }

  private[domains] def findByContentId(id: ContentId): ContT[F, Option[ContentSerializing], Option[ContentSerializing]] = {
    ContT.apply[F, Option[ContentSerializing], Option[ContentSerializing]] { _ =>
      contentSerializingRepository.findByContentId(id).map { cs =>
        cs match {
          case Some(c) =>
            Some(ContentSerializing(c.seriesId, c.contentId))
          case None => None
        }
      }
    }
  }

  private[domains] def upsert(data: Option[ContentSerializing]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { _ =>
      data match {
        case Some(d) => {
          val w = ContentSerializingWriteModel(seriesId = d.seriesId, contentId = d.contentId)
          contentSerializingRepository.bulkUpsert(List(w))
        }
        case None => Monad[F].pure(0)
      }
    }
  }

  private[domains] def bulkUpsert(data: Option[List[ContentSerializing]]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { _ =>
      data match {
        case Some(d) => {
          val ws = d.map(w => ContentSerializingWriteModel(seriesId = w.seriesId, contentId = w.contentId))
          contentSerializingRepository.bulkUpsert(ws)
        }
        case None => Monad[F].pure(0)
      }
    }
  }

  private[domains] def deleteBySeriesId(id: SeriesId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      contentSerializingRepository.deleteBySeriesId(id)
    }
  }

  private[domains] def deleteByContentId(id: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      contentSerializingRepository.deleteByContentId(id)
    }
  }

  private[domains] def delete(seriesId: SeriesId, contentIds: Seq[ContentId]): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      contentSerializingRepository.delete(seriesId, contentIds)
    }
  }

  private[domains] def bulkDelete(data: (SeriesId, Seq[ContentId])): ContT[F, Unit, Unit] = {
    data._2.size match {
      case 0 => ContT.apply[F, Unit, Unit] { _ => Monad[F].pure(()) }
      case _ => this.delete(data._1, data._2)
    }
  }
}
