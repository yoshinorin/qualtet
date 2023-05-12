package net.yoshinorin.qualtet.domains.series

trait SeriesRepository[M[_]] {
  def upsert(data: Series): M[Int]
  def findByName(name: SeriesName): M[Option[Series]]
  def getAll(): M[Seq[Series]]
}
