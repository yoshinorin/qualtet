package net.yoshinorin.qualtet.domains.contentSerializing

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId

class ContentSerializingService[F[_]: Monad](
  contentSerializingRepository: ContentSerializingRepository[F]
) {

  def findBySeriesIdCont(id: SeriesId): ContT[F, Seq[ContentSerializing], Seq[ContentSerializing]] = {
    ContT.apply[F, Seq[ContentSerializing], Seq[ContentSerializing]] { next =>
      contentSerializingRepository.findBySeriesId(id).map { cs =>
        cs.map(c => ContentSerializing(c.seriesId, c.contentId))
      }
    }
  }

  def findByContentIdCont(id: ContentId): ContT[F, Option[ContentSerializing], Option[ContentSerializing]] = {
    ContT.apply[F, Option[ContentSerializing], Option[ContentSerializing]] { next =>
      contentSerializingRepository.findByContentId(id).map { cs =>
        cs match {
          case Some(c) =>
            Some(ContentSerializing(c.seriesId, c.contentId))
          case None => None
        }
      }
    }
  }

  def upsertCont(data: Option[ContentSerializing]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      data match {
        case Some(d) => {
          val w = ContentSerializingWriteModel(seriesId = d.seriesId, contentId = d.contentId)
          contentSerializingRepository.bulkUpsert(List(w))
        }
        case None => contentSerializingRepository.fakeRequestInt
      }
    }
  }

  def bulkUpsertCont(data: Option[List[ContentSerializing]]): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      data match {
        case Some(d) => {
          val ws = d.map(w => ContentSerializingWriteModel(seriesId = w.seriesId, contentId = w.contentId))
          contentSerializingRepository.bulkUpsert(ws)
        }
        case None => contentSerializingRepository.fakeRequestInt
      }
    }
  }

  def deleteBySeriesIdCont(id: SeriesId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentSerializingRepository.deleteBySeriesId(id)
    }
  }

  def deleteByContentIdCont(id: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentSerializingRepository.deleteByContentId(id)
    }
  }

  def deleteCont(seriesId: SeriesId, contentIds: Seq[ContentId]): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentSerializingRepository.delete(seriesId, contentIds)
    }
  }

  def bulkDeleteCont(data: (SeriesId, Seq[ContentId])): ContT[F, Unit, Unit] = {
    data._2.size match {
      case 0 => ContT.apply[F, Unit, Unit] { next => contentSerializingRepository.fakeRequestUnit }
      case _ => this.deleteCont(data._1, data._2)
    }
  }
}
