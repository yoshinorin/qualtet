package net.yoshinorin.qualtet.domains.series

import net.yoshinorin.qualtet.domains.contents.Path

trait SeriesRepository[M[_]] {
  def findByPath(path: Path): M[Option[Series]]
}
