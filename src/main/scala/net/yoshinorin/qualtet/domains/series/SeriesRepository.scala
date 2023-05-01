package net.yoshinorin.qualtet.domains.series

import net.yoshinorin.qualtet.domains.contents.Path

trait SeriesRepository[M[_]] {
  def upsert(data: Series): M[Int]
  def findByPath(path: Path): M[Option[Series]]
  def getAll(): M[Seq[Series]]
}
