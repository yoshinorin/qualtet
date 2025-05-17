package net.yoshinorin.qualtet.domains.series

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentId

class SeriesRepositoryAdapter[F[_]: Monad](
  seriesRepository: SeriesRepository[F]
) {

  private[domains] def upsert(data: Series): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { _ =>
      val w = SeriesWriteModel(id = data.id, name = data.name, path = data.path, title = data.title, description = data.description)
      seriesRepository.upsert(w)
    }
  }

  private[domains] def findById(id: SeriesId): ContT[F, Option[Series], Option[Series]] = {
    ContT.apply[F, Option[Series], Option[Series]] { _ =>
      seriesRepository.findById(id).map { x =>
        x.map { s =>
          Series(s.id, s.name, s.path, s.title, s.description)
        }
      }
    }
  }

  private[domains] def findByName(name: SeriesName): ContT[F, Option[Series], Option[Series]] = {
    ContT.apply[F, Option[Series], Option[Series]] { _ =>
      seriesRepository.findByName(name).map { x =>
        x.map { s =>
          Series(s.id, s.name, s.path, s.title, s.description)
        }
      }
    }
  }

  private[domains] def findByPath(path: SeriesPath): ContT[F, Option[Series], Option[Series]] = {
    ContT.apply[F, Option[Series], Option[Series]] { _ =>
      seriesRepository.findByPath(path).map { x =>
        x.map { s =>
          Series(s.id, s.name, s.path, s.title, s.description)
        }
      }
    }
  }

  private[domains] def findByContentId(id: ContentId): ContT[F, Option[Series], Option[Series]] = {
    ContT.apply[F, Option[Series], Option[Series]] { _ =>
      seriesRepository.findByContentId(id).map { x =>
        x.map { s =>
          Series(s.id, s.name, s.path, s.title, s.description)
        }
      }
    }
  }

  private[domains] def deleteByContentId(id: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      seriesRepository.deleteByContentId(id)
    }
  }

  private[domains] def deleteBySeriesId(id: SeriesId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      seriesRepository.deleteBySeriesId(id)
    }
  }

  private[domains] def fetch: ContT[F, Seq[Series], Seq[Series]] = {
    ContT.apply[F, Seq[Series], Seq[Series]] { _ =>
      seriesRepository.getAll().map { x =>
        x.map { s =>
          Series(s.id, s.name, s.path, s.title, s.description)
        }
      }
    }
  }
}
