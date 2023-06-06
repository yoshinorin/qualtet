package net.yoshinorin.qualtet.domains.contentSerializing

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.series.SeriesId
import net.yoshinorin.qualtet.infrastructure.db.Transactor
import net.yoshinorin.qualtet.syntax._

class ContentSerializingService[M[_]: Monad](
  contentSerializingRepository: ContentSerializingRepository[M]
)(using transactor: Transactor[M]) {

  def findBySeriesIdActions(id: SeriesId): Action[Seq[ContentSerializing]] = {
    Continue(contentSerializingRepository.findBySeriesId(id), Action.done[Seq[ContentSerializing]])
  }

  def bulkUpsertActions(data: Option[List[ContentSerializing]]): Action[Int] = {
    data match {
      case Some(d) => Continue(contentSerializingRepository.bulkUpsert(d), Action.done[Int])
      case None => Continue(contentSerializingRepository.fakeRequestInt, Action.done[Int])
    }
  }

  def deleteBySeriesIdActions(id: SeriesId): Action[Unit] = {
    Continue(contentSerializingRepository.deleteBySeriesId(id), Action.done[Unit])
  }

  def deleteByContentIdActions(id: ContentId): Action[Unit] = {
    Continue(contentSerializingRepository.deleteByContentId(id), Action.done[Unit])
  }

  def deleteActions(seriesId: SeriesId, contentIds: Seq[ContentId]): Action[Unit] = {
    Continue(contentSerializingRepository.delete(seriesId, contentIds), Action.done[Unit])
  }

  def bulkDeleteActions(data: (SeriesId, Seq[ContentId])): Action[Unit] = {
    data._2.size match {
      case 0 => Continue(contentSerializingRepository.fakeRequestUnit, Action.done[Unit])
      case _ => this.deleteActions(data._1, data._2)
    }
  }
}
