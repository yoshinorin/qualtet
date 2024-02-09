package net.yoshinorin.qualtet.domains.series

trait SeriesRepository[F[_]] {
  def upsert(data: Series): F[Int]
  def findByName(name: SeriesName): F[Option[Series]]
  def getAll(): F[Seq[Series]]
}
